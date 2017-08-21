package com.seckill.exception;

/**
 * 重复秒杀异常
 * Created by lc on 2017/8/12.
 */
public class RepeatKillException extends RuntimeException{

    public RepeatKillException(String message) {
        super(message);
    }

    public RepeatKillException(String message, Throwable cause) {
        super(message, cause);
    }


}
