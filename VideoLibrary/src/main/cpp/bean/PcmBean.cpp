//
// Created by Administrator on 2021/9/18 0018.
//

#include "PcmBean.h"

PcmBean::PcmBean(SAMPLETYPE *buffer, int size) {
    this->buffer = (char *) malloc(size);
    this->bufferSize = size;
    memcpy(this->buffer, buffer, size);
}

PcmBean::~PcmBean() {
    if (buffer) {
        free(buffer);
        buffer = nullptr;
    }

}
