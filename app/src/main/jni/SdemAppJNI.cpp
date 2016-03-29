#include <jni.h>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc_c.h>



#define NULL 0

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

};