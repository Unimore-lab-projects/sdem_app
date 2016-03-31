#include <jni.h>

#include <opencv/cv.h>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc_c.h>
//#include <detection.h>#include <iostream>
//#include "opencv2/ts.hpp"
//#include "opencv2/core/private.hpp"
#include "opencv2/imgproc.hpp"
#include "opencv2/imgcodecs.hpp"

#include "opencv2/imgproc/imgproc_c.h"


#include "detection.h"


#define NULL 0

using namespace cv;

extern "C" {
JNIEXPORT void JNICALL
Java_sdem_unimore_com_sdemapp_CameraView_provaJNI(JNIEnv *env, jobject instance, jbyteArray data_) {

    int i = 0;

    jbyte buf = 0,prevByte = 0;
    jbyte *data = env->GetByteArrayElements( data_, NULL);
    jsize length = env->GetArrayLength(data_);

    for(i=0;i<length;i++){
        //buf=data[i];
        //data[i]=data[i]|prevByte;
        //prevByte=buf;
        if(data[i]>127) data[i]=data[i]-127;
        else if(data[i]<127) data[i]=data[i]+127;
    }

    env->ReleaseByteArrayElements( data_, data, 0);
}

JNIEXPORT void JNICALL
Java_sdem_unimore_com_sdemapp_CameraView_detectAndDrawMarkersJNI(JNIEnv *env, jobject instance,
                                                                 jbyteArray data_, jint height_, jint width_) {

    //int i=0;


    jbyte *data = env->GetByteArrayElements(data_, NULL);
    int h=(int) height_;
    int w=(int) width_;
    Mat *m = new Mat(height_+height_/2,width_,CV_8UC1,(uchar*)data);
    cv::cvtColor(*m,*m,CV_YUV2RGB_NV21);
    detection_marker *dm=new detection_marker(DICT_6X6_250);
    dm->detectAndDraw(*m);


    env->ReleaseByteArrayElements(data_, data, 0);
}



};

