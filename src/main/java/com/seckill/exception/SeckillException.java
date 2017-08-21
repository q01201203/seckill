package com.seckill.exception;

/**
 * 秒杀相关的异常
 * Created by lc on 2017/8/12.
 */
public class SeckillException extends RuntimeException {

    public SeckillException(String message) {
        super(message);
    }

    public SeckillException(String message, Throwable cause) {
        super(message, cause);
    }
}
