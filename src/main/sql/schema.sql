-- 数据库初始化脚本

-- 创建数据库
CREATE DATABASE seckill;
-- 使用数据库
use seckill;

-- 创建秒杀库存表
CREATE TABLE seckill (
  'seckill_id' BIGINT       NOT NULL AUTO_INCREMENT COMMENT '商品库存id',
  'name'       VARCHAR(120) NOT NULL COMMENT '商品名称',
  'number'     INT          NOT NULL COMMENT '库存数量',
  'start_time' TIMESTAMP    NOT NULL COMMENT '秒杀开启时间',
  'end_time' timestamp NOT NULL COMMENT '秒杀结束',
  'create_time' timestamp NOT NULL DEFAULT current_timestamp COMMENT '创建时间',
  PRIMARY KEY (seckill_id),
  key idx_start_time(start_time),
  KEY idex_end_time(end_time),
  KEY idex_create_time(create_time)
)ENGINE = InnoDB AUTO_INCREMENT = 1000 DEFAULT CHARSET = utf8 COMMENT = '秒杀库存表';

-- 初始化数据
INSERT INTO
  seckill(name,number,start_time,end_time)
    VALUES
      ('500元秒杀iphone6','100','2017-8-10 21:22:28','2017-8-17 21:22:37'),
      ('1000元秒杀iphone7','100','2017-8-10 21:22:28','2017-8-17 21:22:37'),
      ('1500元秒杀iphone8','100','2017-8-10 21:22:28','2017-8-17 21:22:37'),
      ('2000元秒杀iphone9','100','2017-8-10 21:22:28','2017-8-17 21:22:37')

-- 秒杀成功明细表
-- 用户登陆认证相关的信息
CREATE TABLE success_killed(
  'seckill_id' BIGINT NOT NULL COMMENT '秒杀商品id',
  'user_phone' BIGINT NOT NULL COMMENT '用户手机号',
  'state' TINYINT NOT NULL DEFAULT -1 COMMENT '状态-1无效0成功1已付款',
  'create_time' timestamp NOT NULL DEFAULT current_timestamp COMMENT '创建时间',
  PRIMARY KEY (seckill_id,user_phone),/*联合主键 防止重复秒杀*/
  KEY idex_create_time(create_time)
)ENGINE = InnoDB  DEFAULT CHARSET = utf8 COMMENT = '秒杀库存表';

-- 连接数据库控制台