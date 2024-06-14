#include "CParser.h"
#include "parser.h"
#include <string>

GPSDataUnpacked gpsData;

jdouble getLat(JNIEnv *env, jobject obj)
{
    return static_cast<jdouble>(static_cast<double>(gpsData.lat.DD) + ((static_cast<double>(gpsData.lat.mm) + (static_cast<double>(gpsData.lat._mm) / 100000.0)) / 60.0)) * (gpsData.lat_dir == 'N' ? 1 : -1);
}

jdouble getLon(JNIEnv *env, jobject obj)
{
    return static_cast<jdouble>(static_cast<double>(gpsData.lon.DDD) + ((static_cast<double>(gpsData.lon.mm) + (static_cast<double>(gpsData.lon._mm) / 100000.0)) / 60.0)) * (gpsData.lon_dir == 'E' ? 1 : -1);
}

void parse_raw (JNIEnv *env, jobject obj, jstring raw_str)
{
    jboolean isCopy;
    const char* temp_str = env->GetStringUTFChars(raw_str, &isCopy);
    std::string input_str = std::string(temp_str);
    if(isCopy == JNI_TRUE)
    {
        env->ReleaseStringUTFChars(raw_str, temp_str);
    }
    RMCParser rmcParser (',');
    gpsData = rmcParser.parse_string(input_str);
}