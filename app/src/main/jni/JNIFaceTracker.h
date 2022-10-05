/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_seeta_sdk_FaceTracker */

#ifndef _Included_com_seeta_sdk_FaceTracker
#define _Included_com_seeta_sdk_FaceTracker
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_seeta_sdk_FaceTracker
 * Method:    construct
 * Signature: (Ljava/lang/String;II)V
 */
JNIEXPORT void JNICALL Java_com_example_dovtseetaface6_seeta6_FaceTracker_construct__Ljava_lang_String_2II
  (JNIEnv *, jobject, jstring, jint, jint);

/*
 * Class:     com_seeta_sdk_FaceTracker
 * Method:    construct
 * Signature: (Ljava/lang/String;Ljava/lang/String;III)V
 */
JNIEXPORT void JNICALL Java_com_example_dovtseetaface6_seeta6_FaceTracker_construct__Ljava_lang_String_2Ljava_lang_String_2III
  (JNIEnv *, jobject, jstring, jstring, jint, jint, jint);

/*
 * Class:     com_seeta_sdk_FaceTracker
 * Method:    dispose
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_example_dovtseetaface6_seeta6_FaceTracker_dispose
  (JNIEnv *, jobject);

/*
 * Class:     com_seeta_sdk_FaceTracker
 * Method:    SetSingleCalculationThreads
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_example_dovtseetaface6_seeta6_FaceTracker_SetSingleCalculationThreads
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_seeta_sdk_FaceTracker
 * Method:    Track
 * Signature: (Lcom/seeta/sdk/SeetaImageData;)[Lcom/seeta/sdk/SeetaTrackingFaceInfo;
 */
JNIEXPORT jobjectArray JNICALL Java_com_example_dovtseetaface6_seeta6_FaceTracker_Track
  (JNIEnv *, jobject, jobject);

/*
 * Class:     com_seeta_sdk_FaceTracker
 * Method:    Track
 * Signature: (Lcom/seeta/sdk/SeetaImageData;I)[Lcom/seeta/sdk/SeetaTrackingFaceInfo;
 */
JNIEXPORT jobjectArray JNICALL Java_com_example_dovtseetaface6_seeta6_FaceTracker_Track1
  (JNIEnv *, jobject, jobject, jint);

/*
 * Class:     com_seeta_sdk_FaceTracker
 * Method:    SetMinFaceSize
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_example_dovtseetaface6_seeta6_FaceTracker_SetMinFaceSize
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_seeta_sdk_FaceTracker
 * Method:    GetMinFaceSize
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_example_dovtseetaface6_seeta6_FaceTracker_GetMinFaceSize
  (JNIEnv *, jobject);

/*
 * Class:     com_seeta_sdk_FaceTracker
 * Method:    SetVideoStable
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_com_example_dovtseetaface6_seeta6_FaceTracker_SetVideoStable
  (JNIEnv *, jobject, jboolean);

/*
 * Class:     com_seeta_sdk_FaceTracker
 * Method:    GetVideoStable
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_example_dovtseetaface6_seeta6_FaceTracker_GetVideoStable
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
