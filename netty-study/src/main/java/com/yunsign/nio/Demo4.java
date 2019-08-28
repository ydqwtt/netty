package com.yunsign.nio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Demo4 {
    public static void main(String[] args) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File("E://test.pdf"));
            FileInputStream fileInputStream = new FileInputStream(new File("E://jxbank1_1496916718288.pdf"));

            FileChannel fileInChannel = fileInputStream.getChannel();
            FileChannel fileOutChannel = fileOutputStream.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            while (true){
                byteBuffer.clear();
                int line = fileInChannel.read(byteBuffer);
                System.out.println(line);
                if(line<0){
                    break;
                }
                byteBuffer.flip();
                fileOutChannel.write(byteBuffer);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
