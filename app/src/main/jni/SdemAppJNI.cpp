#include <jni.h>

#include <opencv2/core/core.hpp>
#include "detection.h"


#define NULL 0

using namespace cv;

JNIEXPORT void JNICALL
Java_sdem_unimore_com_sdemapp_CameraView_detectAndDrawMarkersJNI(JNIEnv *env, jobject instance,
                                                                 jbyteArray data_, jint height_,
                                                                 jint width_) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    int h = (int) height_;
    int w = (int) width_;
    Mat *m = new Mat(height_, width_, CV_8UC3, data, CV_AUTO_STEP);
    detection_marker *dm = new detection_marker(DICT_6X6_250);
    dm->detect(*m);

    env->ReleaseByteArrayElements(data_, data, 0);
}

extern "C" {
JNIEXPORT void JNICALL
Java_sdem_unimore_com_sdemapp_CameraView_provaJNI(JNIEnv *env, jobject instance, jbyteArray data_) {

    int i = 0;

    jbyte buf = 0, prevByte = 0;
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    jsize length = env->GetArrayLength(data_);

    for (i = 0; i < length; i++) {
        //buf=data[i];
        //data[i]=data[i]|prevByte;
        //prevByte=buf;
        if (data[i] > 127) data[i] = data[i] - 127;
        else if (data[i] < 127) data[i] = data[i] + 127;
    }

    env->ReleaseByteArrayElements(data_, data, 0);
}


};

