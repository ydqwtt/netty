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
@Slf4j
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
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()) );
            printWriter = new PrintWriter(socket.getOutputStream(),true);
            String expression;
            String result;
            while (true){
                if((expression = bufferedReader.readLine()) == null) {
                    break;
                }
                log.info( "接收到的消息={}", expression);

                result = Calculator.cal(expression);
                System.out.println("result==="+result);
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error( e.getLocalizedMessage() );
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
