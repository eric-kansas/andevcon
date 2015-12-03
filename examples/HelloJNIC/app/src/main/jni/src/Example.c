#include <jni.h>
#include <stdio.h>
#include "com_andevcon_app_Example.h"

JNIEXPORT void JNICALL Java_com_andevcon_app_Example_sayHello(JNIEnv *env, jobject thisObj) {
    printf("Hello World!\n");
    return;
}
