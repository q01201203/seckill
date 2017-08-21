package com.seckill.service.impl;

import com.seckill.dto.Exposer;
import com.seckill.dto.SeckillExecution;
import com.seckill.entity.Seckill;
import com.seckill.exception.RepeatKillException;
import com.seckill.exception.SeckillCloseExcepiton;
import com.seckill.service.SeckillService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by lc on 2017/8/12.
 */
@RunWith(SpringJUnit4ClassRunner.class)
//junit spring的配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml",
"classpath:spring/spring-service.xml"})
public class SeckillServiceImplTest {
    private final Logger logger = LoggerFactory.getLogger(SeckillServiceImplTest.class);

    @Autowired
    private SeckillService seckillService;

    //17:36:58.598 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Closing non transactional SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@39d76cb5]
    @Test
    public void getSeckillList() throws Exception {
        List<Seckill> list = seckillService.getSeckillList();
        logger.info("list={}",list);
    }

    @Test
    public void getById() throws Exception {
        long id = 1000;
        Seckill seckill = seckillService.getById(id);
        logger.info("seckill={}",seckill);
    }

    //17:39:55.290 [main] INFO  c.s.s.impl.SeckillServiceImplTest - exposer=Exposer{exposed=true, md5='b97f08e27201b06a8a4215d4e0e42e6a', seckillId=1000, now=0, start=0, end=0}
    @Test
    public void exportSeckillUrl() throws Exception {
        long id = 1000;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        logger.info("exposer={}",exposer);
    }

    @Test
    public void executeSeckill() throws Exception {
        long id = 1000;
        long phone = 13611112222L;
        String md5 = "b97f08e27201b06a8a4215d4e0e42e6a";
        try {
            SeckillExecution execution = seckillService.executeSeckill(id,phone,md5);
            logger.info("result={}",execution);
        }catch (RepeatKillException e){
            logger.error(e.getMessage());
        }catch (SeckillCloseExcepiton e){
            logger.error(e.getMessage());
        }
    }

    //测试代码完整逻辑，注意可重复测试
    @Test
    public void testSeckillLogic() throws Exception {
        long id = 1000;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        logger.info("exposer={}",exposer);
        if (exposer.isExposed()){
            long phone = 13611112222L;
            String md5 = "b97f08e27201b06a8a4215d4e0e42e6a";
            try {
                SeckillExecution execution = seckillService.executeSeckill(id,phone,md5);
                logger.info("result={}",execution);
            }catch (RepeatKillException e){
                logger.error(e.getMessage());
            }catch (SeckillCloseExcepiton e){
                logger.error(e.getMessage());
            }
        }else{
            logger.warn("exposer={}",exposer);
        }
    }

    //测试代码完整逻辑，注意可重复测试
    @Test
    public void executeSeckillProcedure() throws Exception {
        long id = 1003;
        long phone = 13611113333L;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        if (exposer.isExposed()){
            String md5 = exposer.getMd5();
            SeckillExecution execution = seckillService.executeSeckillProcedure(id,phone,md5);
            logger.info(execution.getStateInfo());
        }

    }
}