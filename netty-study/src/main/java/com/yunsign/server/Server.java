package com.yunsign.server;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Auther: wei.yang.nj
 * @Date: 2019-8-22 19:16
 * @Description:
 */
@Slf4j
public class Server {

    private static final int port = 7777;

    private static ServerSocket serverSocket;

    public static void start() throws IOException{
        log.info( "" );
        start(port);
    }

    private synchronized static void start(int port) throws IOException{
        if(null != serverSocket){
            return;
        }
        try{
            serverSocket = new ServerSocket( port );
            log.info( "服务端已启动，端口："+port );
            while (true){
                Socket socket = serverSocket.accept();
                new Thread( new ServerHandler(socket) ).start();
            }
        }finally {
            if(null != serverSocket){
                log.info( "服务端已关闭" );
                serverSocket.close();
                serverSocket = null;
            }
        }
    }


}
