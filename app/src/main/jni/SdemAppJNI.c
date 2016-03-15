#include <jni.h>

#define NULL 0

JNIEXPORT void JNICALL
Java_sdem_unimore_com_sdemapp_CameraView_provaJNI(JNIEnv *env, jobject instance, jbyteArray data_) {
    jbyte *data = (*env)->GetByteArrayElements(env, data_, NULL);

    // TODO

    (*env)->ReleaseByteArrayElements(env, data_, data, 0);
}