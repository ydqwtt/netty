package com.yunsign.nio;

public class Resp {

    public static final String start = "*";

    public static final String stringLength = "*";

    public static final String line = "\r\n";

    public static enum command{
        SET,GET,INCR
    }
}
