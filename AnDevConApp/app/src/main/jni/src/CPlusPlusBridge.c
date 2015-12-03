#include<stdio.h>
#include<stdlib.h>
#include<ctype.h>

#include "com_andevcon_app_CPlusPlusBridge.h"

JNIEXPORT jstring JNICALL Java_com_andevcon_app_CPlusPlusBridge_getEncryptedID(JNIEnv *env, jobject thiz, jstring id) {

    // "Encrypt" C-string
    char outCStr[] = "ak2p1dmckdafli120dndkl";

    return (*env)->NewStringUTF(env, outCStr);

}