#include <jni.h>

#include <opencv/cv.h>
#include <opencv2/highgui/highgui.hpp>
#include "opencv2/imgproc.hpp"


#include "detection.h"


#define NULL 0

using namespace cv;

extern "C" {


JNIEXPORT void JNICALL
Java_sdem_unimore_com_sdemapp_CameraView_detectJNI(JNIEnv *env, jobject instance, jbyteArray data_,
                                                   jint height, jint width, jintArray nMarker_,
                                                   jintArray idList_, jfloatArray cornerList_) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    jint *nMarker = env->GetIntArrayElements(nMarker_, NULL);
    jint *idList = env->GetIntArrayElements(idList_, NULL);
    jfloat *cornerList = env->GetFloatArrayElements(cornerList_, NULL);

    int i = 0, j = 0;
    Mat *m = new Mat(height, width, CV_8UC1, (uchar *) data);
    detection_marker *dm = new detection_marker(DICT_6X6_250);

    vector<vector<Point2f>> v;
    vector<float> out;
    vector<int> detectedMarkersId;


    dm->detect(*m);
    v = dm->corners();
    detectedMarkersId = dm->ids();

    for (i = 0; i < v.size(); i++) {
        for (j = 0; j < v[i].size(); j++) {
            out.push_back(v[i][j].x);
            out.push_back(v[i][j].y);
        }
    }

    for (i = 0; i < (*nMarker) * 4; i++) {
        cornerList[i] = out[i];
    }

    for (i = 0; i < (*nMarker); i++) {
        idList[i] = detectedMarkersId[i];
    }

//    if ( detectedMarkersId!= static_cast<std::vector<int>>(nullptr) ) {
        *nMarker = detectedMarkersId.size();
    /*} else {
        *nMarker = 0;
    }
*/
    env->ReleaseByteArrayElements(data_, data, 0);
    env->ReleaseIntArrayElements(nMarker_, nMarker, 0);
    env->ReleaseIntArrayElements(idList_, idList, 0);
    env->ReleaseFloatArrayElements(cornerList_, cornerList, 0);
}

/*JNIEXPORT void JNICALL
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
}*/

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
                                                          jfloatArray markers_) {

    int i = 0, j = 0;
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    jfloat *markers = env->GetFloatArrayElements(markers_, NULL);


    Mat *m = new Mat(height, width, CV_8UC1, (uchar *) data);
    //Mat *m2 = new Mat(height, width, CV_8UC3);
    detection_marker *dm = new detection_marker(DICT_6X6_250);

    vector<vector<Point2f>> v;
    vector<float> out;


    //cv::cvtColor(*m, *m2, CV_YUV2RGB_NV21);
    dm->detect(*m);
    v = dm->corners();

    for (i = 0; i < v.size(); i++) {
        for (j = 0; j < v[i].size(); j++) {
            out.push_back(v[i][j].x);
            out.push_back(v[i][j].y);
        }
    }

    for (i = 0; i < out.size(); i++) {
        markers[i] = out[i];
    }

    //markers=out.data();


    /* if(out.size() >0){
         env->SetFloatArrayRegion(markers_, 0,out.size(), markers);
     }*/

    env->ReleaseByteArrayElements(data_, data, 0);
    env->ReleaseFloatArrayElements(markers_, markers, 0);
}

JNIEXPORT jint JNICALL
Java_sdem_unimore_com_sdemapp_CameraView_getMarkersNumber(JNIEnv *env, jobject instance,
                                                          jbyteArray data_, jint height,
                                                          jint width) {
    int i = 0, j = 0;
    jbyte *data = env->GetByteArrayElements(data_, NULL);


    Mat *m = new Mat(height, width, CV_8UC1, (uchar *) data);
    //Mat *m2 = new Mat(height, width, CV_8UC3);
    detection_marker *dm = new detection_marker(DICT_6X6_250);

    vector<vector<Point2f>> v;
    vector<float> out;


//    cv::cvtColor(*m, *m2, CV_YUV2RGB_NV21);
    dm->detect(*m);
    v = dm->corners();

    env->ReleaseByteArrayElements(data_, data, 0);

    return v.size();
}


};

