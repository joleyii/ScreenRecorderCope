//package com.shine.screenrecordercope;
//
//import android.content.res.Configuration;
//
//import java.io.BufferedReader;
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.InetSocketAddress;
//import java.net.ServerSocket;
//import java.net.Socket;
//
//
///**
// * @author ChenYi
// */
//
//public class TCPServer {
//
//    public TCPServer() {
//        try {
//            ServerSocket server = new ServerSocket();
//            server.bind(new InetSocketAddress(Configuration.TCPIP,
//                    Configuration.TCPPort));
//            while (true) {
//                // transfer location change Single User or Multi User
//                TCPServerThread tct = new TCPServerThread(server.accept());
//                tct.start();
//            }
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    public static void main(String[] args) throws IOException {
//        if (args.length == 2) {
//            try {
//                Configuration.TCPIP = args[0];
//                Configuration.TCPPort = Integer.parseInt(args[1]);
//            } catch (Exception ex) {
//                System.exit(1);
//            }
//        }
//        new TCPServer();
//    }
//}
//
//
//
