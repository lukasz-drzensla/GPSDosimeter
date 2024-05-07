#include <jni.h>
#include <string>
#include "radicom/src/CRadicom.h"
#include "RMCParser/CParser.h"

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
extern "C"
JNIEXPORT jintArray JNICALL
Java_pl_edu_agh_gpsdosimeter_JRadicom_q_1memread(JNIEnv *env, jobject thiz) {
    return q_read(env, thiz);
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_pl_edu_agh_gpsdosimeter_RMCParser_getLat(JNIEnv *env, jobject thiz) {
    return getLat(env, thiz);
}
extern "C"
JNIEXPORT void JNICALL
Java_pl_edu_agh_gpsdosimeter_RMCParser_parse_1raw(JNIEnv *env, jobject thiz, jstring raw_str) {
    parse_raw(env, thiz, raw_str);
}
extern "C"
JNIEXPORT jdouble JNICALL
Java_pl_edu_agh_gpsdosimeter_RMCParser_getLon(JNIEnv *env, jobject thiz) {
    return getLon(env, thiz);
}