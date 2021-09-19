//
// Created by Administrator on 2021/9/18 0018.
//

#ifndef WEMUSIC_BUFFERQUEUE_H
#define WEMUSIC_BUFFERQUEUE_H

#include <deque>
#include "../AndroidLog.h"
#include "../PlayStatus.h"
#include "../bean/PcmBean.h"
#include <pthread.h>

using namespace std;

class BufferQueue {

public:
    deque<PcmBean *> queueBuffer;
    pthread_mutex_t mutexBuffer;
    pthread_cond_t condBuffer;
    PlayStatus *playStatus = nullptr;

public:
    BufferQueue(PlayStatus *playStatus);

    ~BufferQueue();

    int putBuffer(SAMPLETYPE *buffer, int size);

    int getBuffer(PcmBean **pPcmBean);

    int clearBuffer();

    void release();

    int getBufferSize();

    int noticeThread();


};


#endif //WEMUSIC_BUFFERQUEUE_H
