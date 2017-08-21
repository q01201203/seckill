package com.seckill.exception;

/**
 * 秒杀关闭异常
 * Created by lc on 2017/8/12.
 */
public class SeckillCloseExcepiton extends RuntimeException{

    public SeckillCloseExcepiton(String message) {
        super(message);
    }

    public SeckillCloseExcepiton(String message, Throwable cause) {
        super(message, cause);
    }
}
