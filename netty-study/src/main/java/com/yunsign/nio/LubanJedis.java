package com.yunsign.nio;

public class LubanJedis {

    LubanSocket lubanSocket = null;

    public LubanJedis(){
        lubanSocket = new LubanSocket("", 10);
    }
    
    
    public void set(String key,String value){

    }

    public String get(String key){
        return null;
    }

    public String incr(String key,String value){
        return null;
    }

    public static String commandUtil(Resp.command  command,String key,String value){
        StringBuilder sb = new StringBuilder();
        return sb.toString();
    }

    public static void main(String[] args) {
         LubanJedis lubanJedis = new LubanJedis();
        lubanJedis.set("","");
    }
}
