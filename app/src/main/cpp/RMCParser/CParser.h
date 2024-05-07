//
// Created by lukasz on 07.05.2024.
//

#ifndef GPSDOSIMETER_CPARSER_H
#define GPSDOSIMETER_CPARSER_H

#include <jni.h>

jdouble getLat(JNIEnv *env, jobject obj);
jdouble getLon(JNIEnv *env, jobject obj);
void parse_raw (JNIEnv *env, jobject obj, jstring raw_str);

#endif //GPSDOSIMETER_CPARSER_H
