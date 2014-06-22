#include "irjni.h"
#include "libsonyir.h"
#include <unistd.h>

#include <android/log.h>

#define LOG_TAG "libsonyir_jni"
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##args)

#define SLEEPTIME 2000

JNIEXPORT jint JNICALL Java_com_sssemil_sonyirremote_ir_IRCommon_startIR
  (JNIEnv * je, jobject jo, jstring js){
   const char *powernode = (*je)->GetStringUTFChars(je, js, 0);
   int status = IRpowerOn(1, powernode);
   usleep(SLEEPTIME);
   status = IRserialOpen();
   usleep(SLEEPTIME);
   IRkickStart();
   usleep(SLEEPTIME);
   (*je)->ReleaseStringUTFChars(je, js, powernode);

   return 1;
}


JNIEXPORT jint JNICALL Java_com_sssemil_sonyirremote_ir_IRCommon_stopIR
  (JNIEnv * je, jobject jo, jstring js){
   const char *powernode = (*je)->GetStringUTFChars(je, js, 0);
   usleep(SLEEPTIME*10);
   int status = IRserialClose();
   usleep(SLEEPTIME);
   status = IRpowerOn(0, powernode);
   (*je)->ReleaseStringUTFChars(je, js, powernode);

   return 1;
}

JNIEXPORT jint JNICALL Java_com_sssemil_sonyirremote_ir_IRCommon_learnKey
  (JNIEnv * je, jobject jo, jstring js){

  const char *filename = (*je)->GetStringUTFChars(je, js, 0);

  int ret = IRlearnKeyToFile(filename);

  LOGI("%s : filename %s\n",__func__,filename);

  (*je)->ReleaseStringUTFChars(je, js, filename);

  return ret;
}


JNIEXPORT jint JNICALL Java_com_sssemil_sonyirremote_ir_IRCommon_sendKey
  (JNIEnv * je, jobject jo, jstring js){

  const char *filename = (*je)->GetStringUTFChars(je, js, 0);

  int ret = IRsendKeyFromFile(filename);

  LOGI("%s : filename %s\n",__func__,filename);

  (*je)->ReleaseStringUTFChars(je, js, filename);

  return ret;
}


JNIEXPORT jint JNICALL Java_com_sssemil_sonyirremote_ir_IRCommon_sendRawKey
  (JNIEnv * je, jobject jo, jstring js, jint ji){

  const char *key = (*je)->GetStringUTFChars(je, js, 0);
  int length = (int) ji;
  int ret = IRsendRawKey(key, length);

  LOGI("%s : key %s\n",__func__,key);

  (*je)->ReleaseStringUTFChars(je, js, key);

  return ret;
}
