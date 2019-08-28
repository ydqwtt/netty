package com.yunsign.server;

import java.io.IOException;
import java.util.Random;

/**
 * @Auther: wei.yang.nj
 * @Date: 2019-8-27 19:39
 * @Description:
 */
public class Test {
    public static void main(String[] args) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Server.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final Random  random = new Random(System.currentTimeMillis());
        final char [] op = new char[]{'+','-','*','/'};
        new Thread(new Runnable() {
            @Override
            public void run() {
                String expression = random.nextInt(10) + "" +op[random.nextInt(4)]
                        + (random.nextInt(10)+1);
                Client.send(expression);
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        }).start();
    }
}
