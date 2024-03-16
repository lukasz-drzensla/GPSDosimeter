#include <jni.h>
#include <string>
#include "CRadicom.h"

extern "C"
JNIEXPORT jstring JNICALL
Java_pl_edu_agh_gpsdosimeter_JRadicom_stringFromJNI(JNIEnv *env, jobject thiz) {
    // TODO: implement stringFromJNI()
    std::string hello = "Hello from radicom :)";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT jintArray JNICALL
Java_pl_edu_agh_gpsdosimeter_JRadicom_q_1read(JNIEnv *env, jobject thiz) {
    return q_read(env, thiz);
}
extern "C"
JNIEXPORT jintArray JNICALL
Java_pl_edu_agh_gpsdosimeter_JRadicom_read_1header(JNIEnv *env, jobject thiz, jintArray frame) {
    return read_hdr(env, thiz, frame);
}
extern "C"
JNIEXPORT jintArray JNICALL
Java_pl_edu_agh_gpsdosimeter_JRadicom_process_1read(JNIEnv *env, jobject thiz, jintArray frame) {
    return process_read(env, thiz, frame);
}
extern "C"
JNIEXPORT jintArray JNICALL
Java_pl_edu_agh_gpsdosimeter_JRadicom_r_1read(JNIEnv *env, jobject thiz) {
    return r_read(env, thiz);
}