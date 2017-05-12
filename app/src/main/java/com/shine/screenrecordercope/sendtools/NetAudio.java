package com.shine.screenrecordercope.sendtools;

import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class NetAudio {
    private Socket client = null;
    private InputStream input = null;
    private OutputStream output = null;
    private String dstName = null;
    private int dstPort = 0;
    private static final String TAG = "NetAudio";
    private boolean isStop = false;
    private Integer in = new Integer(1);
    private boolean isNoMac = false;
    private NetAudioListener mNetAudioListener;
    private AudioPackHeaderUtil util = new AudioPackHeaderUtil();
    private int headerCount = 0;
    private ReceiveThread mReceiveThread;
    private Disposable subReceive = null;

    public void startNet(String dstName, int dstPort, NetAudioListener netAudioListener) {
        mNetAudioListener = netAudioListener;
//        startNet(dstName, dstPort);

    }

    public void startNet(String dstName, int dstPort) {
        Log.d(TAG, "startNet() called with: dstName = [" + dstName + "], dstPort = [" + dstPort + "]");
        this.dstName = dstName;
        this.dstPort = dstPort;
        isStop = false;
        isNoMac = false;
      /*  if (mReceiveThread == null) {
            mReceiveThread = new ReceiveThread();
            mReceiveThread.start();
        }*/
        subReceive = Flowable.create(new FlowableOnSubscribe<byte[]>() {
            @Override
            public void subscribe(FlowableEmitter<byte[]> emmiter) throws Exception {
                while (!emmiter.isCancelled() && !isStop) {
                    byte[] recb = new byte[reciveLeght];
                    if (input != null) {
                        try {
                            if (reciveLeght != 976) {
                                Log.e(TAG, "reciveLeght!=976  " + reciveLeght + "   available  : " + input.available());
                            }
                            int revLenght = input.read(recb, 0, reciveLeght);
                            if (revLenght < reciveLeght) {
                                Log.e(TAG, "revLenght  " + revLenght + "   " + Arrays.toString(recb));
                                reciveLeght = AUDIO_BUFFER - revLenght;
                                continue;
                            }
                            reciveLeght = AUDIO_BUFFER;
                            if (revLenght == 976) {
                                if (Thread.interrupted() || isStop) {
                                    break;
                                }
                                emmiter.onNext(recb);
                            } else {
                                Log.e(TAG, "revLenght != 976  " + revLenght + "   " + Arrays.toString(recb));
                            }
                        } catch (Exception e) {
                            close();
                            SystemClock.sleep(1000);
                            connectSocket();
                            Log.d(TAG, "recieve Exception;---The input stream to read data abnormal, reconnect" + e);
                        }
                    } else {
                        close();
                        SystemClock.sleep(1000);
                        connectSocket();
                        Log.d(TAG, "input= null;---The input stream is null, to reconnect");
                    }
                }
            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .map(new Function<byte[], byte[]>() {
                    @Override
                    public byte[] apply(byte[] bytes) throws Exception {
                        byte[] buffer = new byte[960];
                        System.arraycopy(bytes, 16, buffer, 0, 960);
                        return buffer;
                    }
                }).subscribeOn(Schedulers.io())
                .subscribe(new Consumer<byte[]>() {
                    @Override
                    public void accept(byte[] bytes) throws Exception {
                        if (mNetAudioListener != null) {
                            mNetAudioListener.onReciver(bytes);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "Throwable  :  " + throwable.toString());
                        close();
                        SystemClock.sleep(1000);
                        connectSocket();
                    }
                });

    }


    public void connectSocket() {
        synchronized (in) {
            if (isStop) {
                Log.e(TAG, "If the thread has stopped, not heavy");
                return;
            }
            if (client == null) {
                try {
                    client = new Socket(dstName, dstPort);
                    client.setTcpNoDelay(true);
                    output = client.getOutputStream();
                    input = client.getInputStream();
                } catch (Exception e) {
                    Log.e(TAG, "connectSocket Exception " + e.toString());
                    e.printStackTrace();
                    close();
                    return;
                }
                Log.d(TAG, "socket The connection is successful-->" + client);
            }
        }
    }

    /**
     * 发送音频
     *
     * @param buff
     */
    public boolean sendAudio(byte[] buff) {
        if (buff.length <= 0) return false;
        if (isStop || output == null) return false;
        int lenght = buff.length;
        byte[] sendBuf = new byte[lenght + 16];
        util.initAudio(lenght + 12, headerCount, sendBuf);
        if (isNoMac) {
            buff = new byte[lenght];
        }
        System.arraycopy(buff, 0, sendBuf, 16, buff.length);
        headerCount++;
        if (output != null) {
            try {
                output.write(sendBuf, 0, sendBuf.length);
                output.flush();
                return true;
            } catch (Exception e) {
                Log.e(TAG, "sendAudio Exception  :  " + e.toString());
                close();
                SystemClock.sleep(1000);
                connectSocket();
                return false;
            }
        }
        return false;
    }


    private int reciveLeght = 976;
    private final int AUDIO_BUFFER = 976;

    private class ReceiveThread extends Thread {
        @Override
        public void run() {
        }/*{

            subReceive = Observable.create(new Observable.OnSubscribe<byte[]>() {
                @Override
                public void call(Subscriber<? super byte[]> subscriber) {
                    while ((!Thread.interrupted()) && !isStop){
                        byte[] recb = new byte[reciveLeght];
                        if (input != null){
                            try {
                                if(reciveLeght!=976){
                                    Log.e(TAG,"reciveLeght!=976  "+reciveLeght+"   available  : "+input.available());
                                }
                                int revLenght =input.read(recb, 0, reciveLeght);
                                if(revLenght<reciveLeght){
                                    Log.e(TAG,"revLenght  "+revLenght+"   "+ Arrays.toString(recb));
                                    reciveLeght=AUDIO_BUFFER-revLenght;
                                    continue;
                                }
                                reciveLeght=AUDIO_BUFFER;
                                if (revLenght == 976) {
                                    if (Thread.interrupted() || isStop) {
                                        break;
                                    }
                                    subscriber.onNext(recb);
                                }else {
                                    Log.e(TAG,"revLenght != 976  "+revLenght+"   "+ Arrays.toString(recb));
                                }
                            }catch (Exception e){
                                close();
                                SystemClock.sleep(1000);
                                connectSocket();
                                Log.d(TAG,"recieve Exception;---The input stream to read data abnormal, reconnect"+e);
                            }
                        }else {
                            close();
                            SystemClock.sleep(1000);
                            connectSocket();
                            Log.d(TAG,"input= null;---The input stream is null, to reconnect");
                        }
                    }
                }
            }).map(new Func1<byte[], byte[]>() {
                @Override
                public byte[] call(byte[] bytes) {
                    byte[] buffer= new byte[960];
                    System.arraycopy(bytes, 16, buffer, 0, 960);
//                    mHeaderUtil.initHeader(bytes,buffer);
                    return buffer;
                }
            }).observeOn(Schedulers.io()).onBackpressureDrop().subscribe(new Action1<byte[]>() {
                @Override
                public void call(byte[] bytes) {
                    if(mNetAudioListener!=null){
                        mNetAudioListener.onReciver(bytes);
                    }
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    Log.e(TAG,"Throwable  :  "+throwable.toString());
                    close();
                    SystemClock.sleep(1000);
                    connectSocket();
                }
            });
        }*/
    }

    private AudioReiveHeaderUtil mHeaderUtil = new AudioReiveHeaderUtil();

    public void stopNet() {
        Log.d(TAG, "stopNet() called");
        isStop = true;
        if (subReceive != null) {
            subReceive.dispose();
        }
        close();
        if (mReceiveThread != null) {
            mReceiveThread = null;
        }
    }

    public void close() {
      /*  if ( null!=client) {
            try {
                client.shutdownInput();
            } catch (IOException e) {
                Log.e(TAG, "fail to shundown input : " + e.toString());
                e.printStackTrace();
            }
            try {
                client.shutdownOutput();
            } catch (IOException e) {
                Log.e(TAG, "fail to shundown output : " + e.toString());
                e.printStackTrace();
            }
        }
        if (null!=output) {
            try {
                output.close();
                output = null;
            } catch (IOException e) {
                Log.e(TAG, "IOException : " + e.toString());
                e.printStackTrace();
            }
        }
        if (input != null) {
            try {
                input.close();
                input = null;
            } catch (IOException e) {
                Log.e(TAG, "IOException : " + e.toString());
                e.printStackTrace();
            }
        }*/
        if (null != client) {
            try {
                client.close();
                client = null;
            } catch (IOException e) {
                Log.e(TAG, "fail to lcose client : " + e.toString());
                e.printStackTrace();
            }
        }
    }

    public void setNetAudioListener(NetAudioListener netAudioListener) {
        mNetAudioListener = netAudioListener;
    }


    /**
     * 是否静音
     *
     * @param noMac
     */
    public void setNoMac(boolean noMac) {
        isNoMac = noMac;
    }

    public interface NetAudioListener {
        void onReciver(byte[] bytes);
    }
}
