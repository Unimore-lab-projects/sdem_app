#include <jni.h>

#define NULL 0

JNIEXPORT jbyte* JNICALL
Java_sdem_unimore_com_sdemapp_CameraView_provaJNI(JNIEnv *env, jobject instance, jbyteArray data_) {

    int i = 0;

    jbyte buf = 0,prevByte = 0;
    jbyte *data = (*env)->GetByteArrayElements(env, data_, NULL);
    jsize length = (*env)->GetArrayLength(env,data_);

    for(i=0;i<length;i++){
        //buf=data[i];
        //data[i]=data[i]|prevByte;
        //prevByte=buf;
        if(data[i]>127) data[i]=data[i]-127;
        else if(data[i]<127) data[i]=data[i]+127;
    }

    (*env)->ReleaseByteArrayElements(env, data_, data, 0);

    return data;
}