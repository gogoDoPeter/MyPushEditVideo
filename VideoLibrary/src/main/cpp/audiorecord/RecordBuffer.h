//
// Created by Administrator on 2021/9/21 0021.
//

#ifndef MYPUSHEDITVIDEO_RECORDBUFFER_H
#define MYPUSHEDITVIDEO_RECORDBUFFER_H


class RecordBuffer {
public:
    short **buffer;
    int index = -1;

public:
    RecordBuffer(int buffersize);
    ~RecordBuffer();

    short *getRecordBuffer();

    short * getNowBuffer();
};


#endif //MYPUSHEDITVIDEO_RECORDBUFFER_H
