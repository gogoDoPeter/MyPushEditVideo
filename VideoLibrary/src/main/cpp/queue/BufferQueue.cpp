//
// Created by Administrator on 2021/9/18 0018.
//

#include "BufferQueue.h"

BufferQueue::BufferQueue(PlayStatus *playStatus) {
    this->playStatus = playStatus;
    pthread_mutex_init(&mutexBuffer, nullptr);
    pthread_cond_init(&condBuffer, nullptr);
    LOGD("BufferQueue constructor");
}

BufferQueue::~BufferQueue() {
    pthread_mutex_destroy(&mutexBuffer);
    pthread_cond_destroy(&condBuffer);
    playStatus = nullptr;
    LOGD("BufferQueue Destructor");
}

void BufferQueue::release() {
    noticeThread();
    clearBuffer();
}

int BufferQueue::putBuffer(SAMPLETYPE *buffer, int size) {

    pthread_mutex_lock(&mutexBuffer);

    PcmBean *pPcmBean = new PcmBean(buffer, size);
    queueBuffer.push_back(pPcmBean);
    pthread_cond_signal(&condBuffer);
    pthread_mutex_unlock(&mutexBuffer);
    return 0;
}

int BufferQueue::getBuffer(PcmBean **pPcmBean) {
    LOGD("getBuffer +");
    pthread_mutex_lock(&mutexBuffer);

    while (playStatus != nullptr && !playStatus->exit) {
        LOGD("1 getBuffer ,size=%d",queueBuffer.size());
        if (queueBuffer.size() > 0) {
            *pPcmBean = queueBuffer.front();
            queueBuffer.pop_front();
            break;
        } else {
            if (!playStatus->exit) {
                LOGD("getBuffer ,size=%d pthread_cond_wait",queueBuffer.size());
                pthread_cond_wait(&condBuffer, &mutexBuffer);
            }
        }
        LOGD("2 getBuffer ,size=%d",queueBuffer.size());
    }

    pthread_mutex_unlock(&mutexBuffer);
    return 0;
}

int BufferQueue::getBufferSize() {
    int size = 0;
    pthread_mutex_lock(&mutexBuffer);
    size = queueBuffer.size();
    pthread_mutex_unlock(&mutexBuffer);
    return size;
}

int BufferQueue::clearBuffer() {
    pthread_cond_signal(&condBuffer);
    pthread_mutex_lock(&mutexBuffer);
    while (!queueBuffer.empty()) {
        PcmBean *pcmBean = queueBuffer.front();
        queueBuffer.pop_front();
        delete pcmBean;
        pcmBean = nullptr;
    }
    pthread_mutex_unlock(&mutexBuffer);
    return 0;
}

int BufferQueue::noticeThread() {
    pthread_cond_signal(&condBuffer);
    return 0;
}
