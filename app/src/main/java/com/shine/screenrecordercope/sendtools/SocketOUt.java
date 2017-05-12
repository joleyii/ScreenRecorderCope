package com.shine.screenrecordercope.sendtools;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by 123 on 2017/5/12.
 */

public class SocketOUt {
    Socket socket = null;

    public void sendData(byte[] keyframe, int i) {
        try {
            socket = new Socket("10.0.1.87", 8000);
            // 获取 Client 端的输出流
            BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
            outputStream.write(keyframe, 0, i);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 填充信息
    }
    byte   type;	// VIDEO_HDR_CHAR
    byte   ver;		// VIDEO_HDR_VERSION
    byte   codec;	// VIDEO_CODEC_ID
    byte   extra;
    byte  length;	// 帧数组的长度
    byte  frame;	// frame index
    byte   slice;	// slice index of current frame 当前是第几帧
    int   flast;	// 如果是最后一帧发送1 其余的为0
    byte   flags;

}
