#include <jni.h>
#include "com_myopencvdemo_utils_CVNativeTools.h"
#include <opencv2/core.hpp>
#include <opencv2/objdetect.hpp>
#include <opencv2/opencv.hpp>
#include <map>
#include <string>
#include <vector>
#include <android/log.h>
#include <iostream>
#include <sstream>
using namespace std;
using namespace cv;

#define LOG_TAG "cv"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))



/*
 * Class:     com_myopencvdemo_utils_CVNativeTools
 * Method:    caculatePixNumberFromNative
 * Signature: (J)Ljava/util/TreeMap;
 */
JNIEXPORT jobject JNICALL Java_com_myopencvdemo_utils_CVNativeTools_caculatePixNumberFromNative(JNIEnv *env, jclass claz, jlong addr){

   map<string,int> mapPixs;

   jobject TreeMap;

   jclass class_hashmap = env->FindClass("java/util/TreeMap");
   jmethodID hashmap_init = env->GetMethodID(class_hashmap, "<init>","()V");

   TreeMap = env->NewObject(class_hashmap, hashmap_init, "");


   jmethodID treeMap_put = env->GetMethodID(class_hashmap, "put","(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
   jmethodID treeMap_get = env->GetMethodID(class_hashmap, "get","(Ljava/lang/Object;)Ljava/lang/Object;");
   jmethodID treeMap_contains=env->GetMethodID(class_hashmap, "containsKey","(Ljava/lang/Object;)Z");


   Mat& mat=*(Mat*)addr;
   Mat_<Vec3b>::iterator it = mat.begin<Vec3b>();
   Mat_<Vec3b>::iterator itend = mat.end<Vec3b>();

       long eqCount=0;
       while (it != itend)
       {
           ostringstream oss;

           int b=(int)(*it)[0];
           oss<<b;

           int g=(int)(*it)[1];
           oss<<g;

           int r=(int)(*it)[2];
           oss<<r;

           string key=oss.str();

           int count=mapPixs.count(key);
           if(count==0){
                mapPixs.insert(pair<string, int>(key.c_str(), 1));
           }else{
                int old=mapPixs.find(key.c_str())->second;
                mapPixs.insert(pair<string, int>(key.c_str(),old+1));
           }


           bool exist=(bool)env->CallObjectMethod(TreeMap, treeMap_contains, key.c_str());
           if(exist){
                Integer old=(Integer)env->CallObjectMethod(TreeMap, treeMap_get, key.c_str());
                             env->CallObjectMethod(TreeMap, treeMap_put, key.c_str(),1+old);
           }else{
                             env->CallObjectMethod(TreeMap, treeMap_put, key.c_str(),1);
           }

        it++;
       }


     return TreeMap;

 }

