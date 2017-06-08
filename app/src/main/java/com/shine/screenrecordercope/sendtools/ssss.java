//package com.shine.screenrecordercope.sendtools;
//
//import static android.R.attr.key;
//
///**
// * Created by 123 on 2017/5/17.
// */
//
//public class ssss {
//
//    int sendVideo(unsigned char*buf, int size, unsigned char*m_pVideoPacket) {
//        Shine_VideoPackHdr_Header m_pVideoHeader;
//        memset( & m_pVideoHeader, 0, sizeof(Shine_VideoPackHdr_Header));
//        m_pVideoHeader.type = VIDEO_HDR_CHAR;
//        'V'
//        m_pVideoHeader.ver = VIDEO_HDR_VERSION;
//        2
//        m_pVideoHeader.codec = VICODEC_H264;
//        0
//        m_pVideoHeader.length = 0;
//        整个帧的长度
//        m_pVideoHeader.frame = 0;
//        第几帧
//        m_pVideoHeader.slice = 0;
//        第几包
//        m_pVideoHeader.flast = 1;
//        最后一包 1
//        m_pVideoHeader.flags = IFlag;
//        key 1
//        unsigned char *pcPacket = buf;
//        int nHeaderLen = sizeof(Shine_VideoPackHdr_Header);
//        int nSendLen;
//        int nRemainLen = size;//计算未发送的数据
//        m_pVideoHeader.length = size;//发送的数据的大小
//        m_pVideoHeader.frame = m_unCurrentVideoFrames;//发送的帧数
//        if (MAXFRAMES > m_unCurrentVideoFrames)
//            m_unCurrentVideoFrames++;
//        else
//            m_unCurrentVideoFrames = 0;
//        int count_video = 0;
//        m_pVideoHeader.flast = 0;//发送结束标记 1：发送结束
//        while (0 < nRemainLen) {
//            nSendLen = PACKET_LEN;
//            1048
//            if (nRemainLen > (nSendLen - nHeaderLen)) {
//                nRemainLen -= (nSendLen - nHeaderLen);
//            } else {
//                nSendLen = nRemainLen + nHeaderLen;
//                nRemainLen = 0;
//                m_pVideoHeader.flast = 1;
//            }
//            m_pVideoHeader.slice = count_video;//统计一帧中的包
//            memset(m_pVideoPacket, 0, PACKET_LEN);
//            memcpy(m_pVideoPacket, & m_pVideoHeader, nHeaderLen);
//            memcpy(m_pVideoPacket + nHeaderLen, pcPacket, nSendLen - nHeaderLen);
//            int retValue = 0;
//            int count = 0;
//            DWORD tcpDataFlag = 0xDAC;
//            tcpDataFlag = (tcpDataFlag << 20) + nSendLen;
//            //printf(" tcpDataFlag = %x \n",tcpDataFlag);
//            if (new_fd > 0) {
//                pthread_mutex_lock( & socketlock);
//                retValue = send(new_fd, & tcpDataFlag, 4, 0);
//                if (retValue < 0) {
//                    conFlag = false;
//                    if (new_fd > 0) {
//                        close(new_fd);
//                        new_fd = -1;
//                    }
//                    pthread_mutex_unlock( & socketlock);
//                    perror("send Video failed");
//                    return -1;
//                }
//                retValue = 0;
//                while (count != nSendLen) {
//                    retValue = send(new_fd, m_pVideoPacket + count, nSendLen - count, 0);
//                    if (retValue < 0) {
//                        conFlag = false;
//                        if (new_fd > 0) {
//                            close(new_fd);
//                            new_fd = -1;
//                        }
//                        pthread_mutex_unlock( & socketlock);
//                        perror("send Video failed");
//                        return -1;
//                    }
//                    count = count + retValue;
//                }
//                pthread_mutex_unlock( & socketlock);
//                count_video++;
//                pcPacket += (nSendLen - nHeaderLen);
//                memset(m_pVideoPacket, 0, sizeof(m_pVideoPacket));
//            } else {
//                break;
//            }
//        }
//        pcPacket = NULL;
//        return 1;
//    }
//}
