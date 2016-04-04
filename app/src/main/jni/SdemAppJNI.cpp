#include <jni.h>

#include <opencv/cv.h>
#include <opencv2/highgui/highgui.hpp>
#include "opencv2/imgproc.hpp"


#include "detection.h"


#define NULL 0


using namespace cv;

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

JNIEXPORT void JNICALL
Java_sdem_unimore_com_sdemapp_CameraView_detectAndDrawMarkersJNI(JNIEnv *env, jobject instance,
                                                                 jbyteArray data_, jint height_,
                                                                 jint width_) {

    //int i=0;


    jbyte *data = env->GetByteArrayElements(data_, NULL);
    Mat *m = new Mat(height_ + height_ / 2, width_, CV_8UC1, (uchar *) data);
    Mat *m2 = new Mat(height_, width_, CV_8UC3);
    detection_marker *dm = new detection_marker(DICT_6X6_250);

    cv::cvtColor(*m, *m2, CV_YUV2RGB_NV21);
    dm->detectAndDraw(*m);


    env->ReleaseByteArrayElements(data_, data, 0);
}
JNIEXPORT void JNICALL
Java_sdem_unimore_com_sdemapp_CameraView_detectMarkersJNI(JNIEnv *env, jobject instance,
                                                       jbyteArray data_, jint height, jint width,
                                                       jfloatArray markers_,
                                                       jintArray markersSize_) {

    int i = 0, j = 0;
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    //jfloat *markers = env->GetFloatArrayElements(markers_, NULL);
    jfloat *markers;
    jint *ids = env->GetIntArrayElements(markersSize_, NULL);


    Mat *m = new Mat(height + height / 2, width, CV_8UC1, (uchar *) data);
    Mat *m2 = new Mat(height, width, CV_8UC3);
    detection_marker *dm = new detection_marker(DICT_6X6_250);

    vector<vector<Point2f>> v;
    vector<float> out;
    vector<int> idsarray;

    cv::cvtColor(*m, *m2, CV_YUV2RGB_NV21);
    dm->detect(*m);
    v = dm->corners();
    idsarray = dm->ids();

    for (i = 0; i < v.size(); i++) {
        for (j = 0; j < v[i].size(); j++) {
            out.push_back(v[i][j].x);
            out.push_back(v[i][j].y);
        }
    }

  /*  for (i = 0; i < out.size(); i++) {
        markers[i] = out[i];
    }*/
    markers=out.data();

    /*for (int i = 0; i < idsarray.size(); i++) {
        ids[i]=idsarray[i];
    }*/

    env->SetFloatArrayRegion(markers_, 0,out.size(), markers);

    env->ReleaseByteArrayElements(data_, data, 0);
    env->ReleaseFloatArrayElements(markers_, markers, 0);
}


};

