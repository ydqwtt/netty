package com.yunsign.server;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @Auther: wei.yang.nj
 * @Date: 2019-8-27 19:13
 * @Description:
 */
@Slf4j
public class Client {

    private static final int DEFAULT_SERVER_PORT = 7777;

    private static final String DEFAULT_SERVER_IP = "127.0.0.1";

    public static void send(String expression){
        send(DEFAULT_SERVER_PORT,expression);
    }

    private static void send(int defaultServerPort, String expression) {

        Socket socket = null;

        BufferedReader bufferedReader = null;

        PrintWriter printWriter = null;

        try{
            socket = new Socket(DEFAULT_SERVER_IP,DEFAULT_SERVER_PORT);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printWriter = new PrintWriter(socket.getOutputStream(),true);
            System.out.println("表达式为:"+expression);
//            System.out.println("读取信息:"+bufferedReader.readLine());
        }catch (Exception e){
            e.printStackTrace();
        }finally {

        }
    }
}
