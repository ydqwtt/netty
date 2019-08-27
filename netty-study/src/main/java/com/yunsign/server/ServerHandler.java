package com.yunsign.server;

import com.sun.javafx.css.CalculatedValue;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @Auther: wei.yang.nj
 * @Date: 2019-8-22 19:42
 * @Description:
 */
public class ServerHandler implements Runnable {

    private Socket socket;
    public ServerHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        BufferedReader bufferedReader = null;
        PrintWriter printWriter = null;
        try {
            System.out.println("111");
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()) );
            System.out.println("222");
            printWriter = new PrintWriter(socket.getOutputStream(),true);
            System.out.println("333");
            String expression;
            String result;
            System.out.println("444");
            while (true){
                System.out.println("555"+bufferedReader.readLine());
                if((expression = bufferedReader.readLine()) == null) {
                    System.out.println("222");
                    break;
                }

                System.out.println(("接收到的消息=" + expression));

                result = Calculator.cal(expression);
                System.out.println("result==="+result);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(null != bufferedReader){
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bufferedReader = null;
            }
            if (null != printWriter){
                printWriter.close();
                printWriter= null;
            }
            if(null != socket){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                socket = null;
            }
        }
    }
}
