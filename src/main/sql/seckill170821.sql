/*
Navicat MySQL Data Transfer

Source Server         : lic
Source Server Version : 50717
Source Host           : localhost:3306
Source Database       : seckill

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2017-08-21 23:31:30
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `seckill`
-- ----------------------------
DROP TABLE IF EXISTS `seckill`;
CREATE TABLE `seckill` (
  `seckill_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商品库存id',
  `name` varchar(120) NOT NULL COMMENT '商品名称',
  `number` int(11) NOT NULL COMMENT '库存数量',
  `start_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '秒杀开启时间',
  `end_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '秒杀结束',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`seckill_id`),
  KEY `idx_start_time` (`start_time`),
  KEY `idex_end_time` (`end_time`),
  KEY `idex_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1004 DEFAULT CHARSET=utf8 COMMENT='秒杀库存表';

-- ----------------------------
-- Records of seckill
-- ----------------------------
INSERT INTO `seckill` VALUES ('1000', '500元秒杀iphone6', '96', '2017-08-17 00:15:05', '2017-08-17 21:22:37', '2017-08-10 21:34:22');
INSERT INTO `seckill` VALUES ('1001', '1000元秒杀iphone7', '100', '2017-08-10 21:22:28', '2017-08-17 21:22:37', '2017-08-10 21:34:22');
INSERT INTO `seckill` VALUES ('1002', '1500元秒杀iphone8', '100', '2017-08-10 21:22:28', '2017-08-17 21:22:37', '2017-08-10 21:34:22');
INSERT INTO `seckill` VALUES ('1003', '2000元秒杀iphone9', '98', '2017-08-21 23:23:10', '2017-08-22 21:22:37', '2017-08-10 21:34:22');

-- ----------------------------
-- Table structure for `success_killed`
-- ----------------------------
DROP TABLE IF EXISTS `success_killed`;
CREATE TABLE `success_killed` (
  `seckill_id` bigint(20) NOT NULL COMMENT '秒杀商品id',
  `user_phone` bigint(20) NOT NULL COMMENT '用户手机号',
  `state` tinyint(4) NOT NULL DEFAULT '-1' COMMENT '状态-1无效0成功1已付款',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`seckill_id`,`user_phone`),
  KEY `idex_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='秒杀库存表';

-- ----------------------------
-- Records of success_killed
-- ----------------------------
INSERT INTO `success_killed` VALUES ('1000', '13611112222', '0', '2017-08-12 17:43:35');
INSERT INTO `success_killed` VALUES ('1000', '13611114444', '-1', '2017-08-12 15:26:44');
INSERT INTO `success_killed` VALUES ('1000', '13618393409', '0', '2017-08-17 00:15:05');
INSERT INTO `success_killed` VALUES ('1001', '13611114444', '0', '2017-08-12 15:32:22');
INSERT INTO `success_killed` VALUES ('1003', '13611112222', '-1', '2017-08-21 22:46:55');
INSERT INTO `success_killed` VALUES ('1003', '13611113333', '-1', '2017-08-21 23:23:10');

-- ----------------------------
-- Procedure structure for `execute_seckill`
-- ----------------------------
DROP PROCEDURE IF EXISTS `execute_seckill`;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `execute_seckill`(in v_seckill_id bigint, in v_phone bigint,
    in v_kill_time timestamp,out r_result int)
BEGIN
    DECLARE insert_count int default 0;
    START TRANSACTION ;
    insert ignore INTO success_killed
      (seckill_id, user_phone,state,create_time)
      VALUES (v_seckill_id,v_phone,0,v_kill_time);
    SELECT row_count() INTO  insert_count;
    IF (insert_count = 0) THEN
      ROLLBACK ;
      set r_result = -1;
    ELSEIF (insert_count < 0) THEN
      ROLLBACK ;
      set r_result = -2;
    ELSE
      UPDATE  seckill.seckill
      set number = number -1
      WHERE seckill_id = v_seckill_id
        AND end_time > v_kill_time
        AND start_time <v_kill_time
        AND number >0;
        SELECT row_count() INTO insert_count;
        IF (insert_count = 0 )THEN
          ROLLBACK ;
          set r_result = 0;
        ELSEIF (insert_count <0 )THEN
          ROLLBACK ;
          set r_result = -2;
        ELSE
          COMMIT ;
          set r_result = 1;
        END IF;
    END IF;
  END
;;
DELIMITER ;
