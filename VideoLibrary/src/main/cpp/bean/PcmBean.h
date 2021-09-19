//
// Created by Administrator on 2021/9/18 0018.
//

#ifndef WEMUSIC_PCMBEAN_H
#define WEMUSIC_PCMBEAN_H

#include <SoundTouch.h>

using namespace soundtouch;

class PcmBean {
public:
    char *buffer;
    int bufferSize;

public:
    PcmBean(SAMPLETYPE *buffer, int size);

    ~PcmBean();
};


#endif //WEMUSIC_PCMBEAN_H
