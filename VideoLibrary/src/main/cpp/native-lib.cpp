#include <jni.h>
#include <string>
#include "AndroidLog.h"
#include "CallJava.h"
#include "FFmpeg.h"

#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include "audiorecord/RecordBuffer.h"
#include "AndroidLog.h"

_JavaVM *javaVm = nullptr;
CallJava *callJava = nullptr;
FFmpeg *fFmpeg = nullptr;
PlayStatus *playStatus = nullptr;
bool isStopDone = true;
pthread_t thread_start;


//todo for OpenSL ES record audio
SLObjectItf slObjectEngine = NULL;
SLEngineItf engineItf = NULL;
SLObjectItf recordObj = NULL;
SLRecordItf recordItf = NULL;
SLAndroidSimpleBufferQueueItf recorderBufferQueue = NULL;
RecordBuffer *recordBuffer = nullptr;
FILE *pcmFile = NULL;
bool finish = false;

extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
{
    jint result = -1;

    javaVm = vm;
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK)
    {
        LOGE("GetEnv fail");
        return result;
    }
    return JNI_VERSION_1_6;
}

void *myStartCallback(void *data)
{
    FFmpeg *fFmpeg = static_cast<FFmpeg *>(data);//WlFFmpeg *fFmpeg = (WlFFmpeg *) data;
    fFmpeg->startDecode();
    pthread_exit(&thread_start);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_peter_videolibrary_player_AudioPlayer_nativeSeek(JNIEnv *env, jobject thiz, jint seconds)
{
    if (fFmpeg != nullptr)
    {
        fFmpeg->seek(seconds);
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_peter_videolibrary_player_AudioPlayer_nativeGetDuration(JNIEnv *env, jobject thiz)
{
    if (fFmpeg != nullptr)
    {
        return fFmpeg->duration;
    }
    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_peter_videolibrary_player_AudioPlayer_nativeSetVolume(JNIEnv *env, jobject thiz, jint percent)
{
    if (fFmpeg != nullptr)
    {
        fFmpeg->setVolume(percent);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_peter_videolibrary_player_AudioPlayer_nativeSetMute(JNIEnv *env, jobject thiz, jint muteType)
{

    if (fFmpeg != nullptr)
    {
        fFmpeg->setMute(muteType);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_peter_videolibrary_player_AudioPlayer_nativeSetSpeed(JNIEnv *env, jobject thiz, jdouble speed)
{
    if (fFmpeg != nullptr)
    {
        fFmpeg->setSpeed(speed);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_peter_videolibrary_player_AudioPlayer_nativeSetPitch(JNIEnv *env, jobject thiz, jdouble pitch)
{
    if (fFmpeg != nullptr)
    {
        fFmpeg->setPitch(pitch);
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_peter_videolibrary_player_AudioPlayer_nativeGetSampleRate(JNIEnv *env, jobject thiz)
{
    if (fFmpeg != nullptr)
    {
        return fFmpeg->getAudioSampleRate();
    }
    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_peter_videolibrary_player_AudioPlayer_nativeStartStopRecord(JNIEnv *env, jobject thiz, jboolean b_start_record)
{
    if (fFmpeg != nullptr)
    {
        fFmpeg->startRecord(b_start_record);
    }
}

/*
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_peter_myplayer_player_WeAudioPlayer_nativeCutAudioPlay(JNIEnv *env, jobject thiz, jint start_time, jint end_time, jboolean is_show_pcm)
{
    if (fFmpeg != nullptr)
    {
        return fFmpeg->cutAudioPlay(start_time,end_time,is_show_pcm);
    }
    return false;
}*/

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_peter_videolibrary_player_AudioPlayer_nativeCutAudioPlay(JNIEnv *env, jobject thiz, jdouble start_time, jdouble end_time, jboolean is_show_pcm)
{
    if (fFmpeg != nullptr)
    {
        return fFmpeg->cutAudioPlay(start_time,end_time,is_show_pcm);
    }
    return false;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_peter_videolibrary_player_AudioPlayer_nativeStart(JNIEnv *env, jobject thiz) {
    LOGD("native_start start +");
    if (fFmpeg != nullptr)
    {
//        fFmpeg->start();
        pthread_create(&thread_start, NULL, myStartCallback, fFmpeg);

    }
    LOGD("native_start start -");
}

extern "C"
JNIEXPORT void JNICALL
Java_com_peter_videolibrary_player_AudioPlayer_nativePrepared(JNIEnv *env, jobject thiz,
                                                              jstring source_) {
    LOGD("prepared +");
    const char *source = env->GetStringUTFChars(source_, 0);
    LOGD("prepared, source:%s", source);
    if (fFmpeg == nullptr)
    {
        if (callJava == nullptr)
        {
            callJava = new CallJava(javaVm, env, &thiz);
        }
        playStatus = new PlayStatus();
        fFmpeg = new FFmpeg(source, callJava, playStatus);
        LOGD("fFmpeg->prepared");
        fFmpeg->prepared();
    }

    LOGD("prepared -");
}

extern "C"
JNIEXPORT void JNICALL
Java_com_peter_videolibrary_player_AudioPlayer_nativePause(JNIEnv *env, jobject thiz) {
    if (fFmpeg != nullptr)
    {
        fFmpeg->pause();
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_peter_videolibrary_player_AudioPlayer_nativeResume(JNIEnv *env, jobject thiz) {

    if (fFmpeg != nullptr)
    {
        fFmpeg->resume();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_peter_videolibrary_player_AudioPlayer_nativeStop(JNIEnv *env, jobject instance) {
    if (!isStopDone)
    {// TODO 解决退出过程中又点击stop，导致流程走多次异常
        return;
    }

    jclass jClaz = env->GetObjectClass(instance);
    jmethodID jmid_next = env->GetMethodID(jClaz, "onCallNext", "()V");

    isStopDone = false;

    //TODO 注意：内存释放原则，哪里申请的，哪里释放，其他地方使用的话，其他地方释放时置位NULL
    if (fFmpeg != nullptr)
    {
        fFmpeg->release();
        delete (fFmpeg); //Call ffmpeg destructor
        fFmpeg = nullptr;

        if (callJava != nullptr)
        {
            delete (callJava);//Call callJava destructor
            callJava = nullptr;
        }
        if (playStatus != nullptr)
        {
            delete (playStatus);//Call playStatus destructor
            playStatus = nullptr;
        }
    }

    isStopDone = true;
    env->CallVoidMethod(instance, jmid_next);
}

int readSize=0;

//todo 注册的 callback : bqRecorderCallback; 设置开始录制后，当recordBuffer设置满后，会回调 bqRecorderCallback
void bqRecorderCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
    if(pcmFile){
        readSize = fwrite(recordBuffer->getNowBuffer(), 1, 4096, pcmFile);
//        LOGE("recording , readSize:%s",readSize);
        LOGE("recording ");
    }
    if (finish) {
        LOGE("录制完成");
        (*recordItf)->SetRecordState(recordItf, SL_RECORDSTATE_STOPPED);
        //TODO 释放 SL 资源
        (*recordObj)->Destroy(recordObj);
        recordObj = NULL;
        recordItf = NULL;
        (*slObjectEngine)->Destroy(slObjectEngine);
        slObjectEngine = NULL;
        engineItf = NULL;
        delete (recordBuffer);
        recordBuffer = nullptr;

        fclose(pcmFile);

    } else {
        LOGE("正在录制");
        //做入队操作，当 recorderBufferQueue 填满后，会回调 bqRecorderCallback
        (*recorderBufferQueue)->Enqueue(recorderBufferQueue, recordBuffer->getRecordBuffer(), 4096);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_peter_videolibrary_player_AudioPlayer_nativeStartRecordPcm2(JNIEnv *env, jobject thiz,
                                                                     jstring path_) {
    LOGD("start Record audio +");
    if (finish) {
        return;
    }
    const char *path = env->GetStringUTFChars(path_, 0);
    LOGD("start Record audio, finish=%d, path:%s",finish,path);
    finish = false;
    pcmFile = fopen(path, "w");
    if(pcmFile){
        LOGE("Open pcm file success");
    }else{
        LOGE("Open pcm file fail");
        fclose(pcmFile);
    }
    recordBuffer = new RecordBuffer(4096);

    //todo 步骤1：三部曲，为了通过slObjectEngine 得到engineItf
    slCreateEngine(&slObjectEngine, 0, NULL, 0, NULL, NULL);
    (*slObjectEngine)->Realize(slObjectEngine, SL_BOOLEAN_FALSE);
    (*slObjectEngine)->GetInterface(slObjectEngine, SL_IID_ENGINE, &engineItf);


    SLDataLocator_IODevice loc_dev = {SL_DATALOCATOR_IODEVICE,
                                      SL_IODEVICE_AUDIOINPUT,
                                      SL_DEFAULTDEVICEID_AUDIOINPUT,
                                      NULL};
    SLDataSource audioSrc = {&loc_dev, NULL};


    SLDataLocator_AndroidSimpleBufferQueue loc_bq = {
            SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,
            2
    };


    SLDataFormat_PCM format_pcm = {
            SL_DATAFORMAT_PCM, 2, SL_SAMPLINGRATE_44_1,
            SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT, SL_BYTEORDER_LITTLEENDIAN
    };

    SLDataSink audioSnk = {&loc_bq, &format_pcm};

    //todo 如果要加均衡器功能，也是这里添加扩展
    const SLInterfaceID id[1] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE};
    const SLboolean req[1] = {SL_BOOLEAN_TRUE};

    //todo 步骤2： 通过 engineItf 得到 recordObj , 然后使用 recordObj （三部曲） 得到 recorderBufferQueue
    (*engineItf)->CreateAudioRecorder(engineItf, &recordObj, &audioSrc, &audioSnk, 1, id, req);
    (*recordObj)->Realize(recordObj, SL_BOOLEAN_FALSE);
    (*recordObj)->GetInterface(recordObj, SL_IID_RECORD, &recordItf);

    (*recordObj)->GetInterface(recordObj, SL_IID_ANDROIDSIMPLEBUFFERQUEUE, &recorderBufferQueue);

    //todo 步骤3：Enqueue 获取RecordBuffer
    // 入队设置队列大小
    (*recorderBufferQueue)->Enqueue(recorderBufferQueue, recordBuffer->getRecordBuffer(), 4096);

    //todo 步骤4：注册callback， bqRecorderCallback; 设置开始录制后，当recordBuffer设置满后，会回调 bqRecorderCallback
    (*recorderBufferQueue)->RegisterCallback(recorderBufferQueue, bqRecorderCallback, NULL);
    //todo 步骤5：通过 recordItf 设置 SL_RECORDSTATE_RECORDING
    (*recordItf)->SetRecordState(recordItf, SL_RECORDSTATE_RECORDING);

    env->ReleaseStringUTFChars(path_, path);
    LOGD("start Record audio -");
}

extern "C"
JNIEXPORT void JNICALL
Java_com_peter_videolibrary_player_AudioPlayer_nativeStopRecordPcm2(JNIEnv *env, jobject thiz) {
    finish = true;
    LOGD("stop Record audio, finish:%d",finish);
}