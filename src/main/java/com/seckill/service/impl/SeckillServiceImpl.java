package com.seckill.service.impl;

import com.seckill.dao.SeckillDao;
import com.seckill.dao.SuccessKilledDao;
import com.seckill.dao.cache.RedisDao;
import com.seckill.dto.Exposer;
import com.seckill.dto.SeckillExecution;
import com.seckill.entity.Seckill;
import com.seckill.entity.SuccessKilled;
import com.seckill.enums.SeckillStatEnum;
import com.seckill.exception.RepeatKillException;
import com.seckill.exception.SeckillCloseExcepiton;
import com.seckill.exception.SeckillException;
import com.seckill.service.SeckillService;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lc on 2017/8/12.
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    //注入service依赖
    @Autowired
    private SeckillDao seckillDao;
    @Autowired
    private SuccessKilledDao successKilledDao;
    @Autowired
    private RedisDao redisDao;

    //用于混淆md5
    private final String slat = "sdlfjlsdkfjlklsdjfl@jsdljf(*sdjf";

    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,4);
    }

    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    public Exposer exportSeckillUrl(long seckillId) {
        //缓存优化 ,超时的基础上维护一致性
        //1访问redis
        Seckill  seckill = redisDao.getSeckill(seckillId);
        if (seckill==null){
            //2访问数据库
            seckill = seckillDao.queryById(seckillId);
            if (seckill == null){
                return new Exposer(false,seckillId);
            }else {
                //3放入redis
                redisDao.putSeckill(seckill);
            }
        }

        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date nowTime = new Date();
        if (nowTime.getTime()<startTime.getTime()||
                nowTime.getTime()>endTime.getTime()){
            return new Exposer(false,seckillId,nowTime.getTime(),startTime.getTime(),endTime.getTime());
        }

        String md5 = getMD5(seckillId);
        return new Exposer(true,md5,seckillId);
    }

    private String getMD5(long seckillId){
        String base = seckillId+"/"+slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    /**
     * 使用注解控制事务方法的优点：
     * 1：开发团队打成一致的约定，明确标注事务方法的编程风格
     * 2：保证事务方法的执行方法的时间尽可能的短，不要穿插其他的网络操作（RPC/HTTP请求或者剥离到事务方法外部）
     * 3：不是所有的方法都需要事务，如只有一条修改操作，只读操作不需要事务控制
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     * @throws SeckillException
     * @throws RepeatKillException
     * @throws SeckillCloseExcepiton
     */
    @Transactional
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseExcepiton {
        if (md5 == null || !md5.equals(getMD5(seckillId))){
            throw new SeckillException("seckill data rewrite");
        }

        //执行秒杀逻辑：减库存+记录购买行为
        Date nowTime = new Date();

        try {
            //记录购买行为
            int insertCount = successKilledDao.insertSuccessKilled(seckillId,userPhone);
            //唯一：seckillId,userPhone
            if (insertCount <=0){
                //重复秒杀
                throw new RepeatKillException("seckill repeated");
            }else {
                //减库存  热点商品竞争
                int updateCount = seckillDao.reduceNumber(seckillId,nowTime);
                if (updateCount<=0){
                    //没有更新到记录，秒杀结束 rollback
                    throw new SeckillCloseExcepiton("seckill is closed");
                }else{
                    //秒杀成功  commit
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                    return  new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS,successKilled);
                }
            }
        }catch (SeckillCloseExcepiton e){
            throw e;
        }catch (RepeatKillException e){
            throw e;
        } catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new SeckillException("seckill inner error:"+e.getMessage());
        }
    }

    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) {
        if (md5 == null || !md5.equals(getMD5(seckillId))){
            return new SeckillExecution(seckillId,SeckillStatEnum.DATA_REWRITE);
        }
        Date killTime = new Date();
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("seckillId",seckillId);
        map.put("phone",userPhone);
        map.put("killTime",killTime);
        map.put("result",null);
        //执行存储过程，result被复制
        try {
            seckillDao.killByProcedure(map);
            //获取result
            int result  = MapUtils.getInteger(map,"result",-2);
            if (result == 1){
                SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                return  new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS,successKilled);
            }else{
                return new SeckillExecution(seckillId,SeckillStatEnum.stateOf(result));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return  new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
        }
    }
}
