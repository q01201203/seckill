package com.seckill.dao;

import com.seckill.entity.SuccessKilled;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * Created by lc on 2017/8/12.
 */
@RunWith(SpringJUnit4ClassRunner.class)
//junit spring的配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SuccessKilledDaoTest {

    @Resource
    private SuccessKilledDao successKilledDao;

    @Test
    public void insertSuccessKilled() throws Exception {
        long id  = 1001L;
        long phone = 13611114444L;
        int count = successKilledDao.insertSuccessKilled(id,phone);
        System.out.println("count" +count);
    }

    @Test
    public void queryByIdWithSeckill() throws Exception {
        long id  = 1001L;
        long phone = 13611114444L;
        SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(id,phone);
        System.out.println(successKilled);
    }

}