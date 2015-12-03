
LOCAL_PATH := $(call my-dir)

## Build the shared library.
include $(CLEAR_VARS)
LOCAL_MODULE            := build_andevcon_shared
LOCAL_MODULE_FILENAME   := libandevcon-example
LOCAL_SRC_FILES         := src/Example.c
LOCAL_CFLAGS            := -DANDEVCON_SHARED=1
include $(BUILD_SHARED_LIBRARY)

## Build the static library.
include $(CLEAR_VARS)
LOCAL_MODULE            := build_andevcon_static
LOCAL_MODULE_FILENAME   := libandevcon-example
LOCAL_SRC_FILES         := src/Example.c
LOCAL_CFLAGS            := -DANDEVCON_STATIC=1
include $(BUILD_STATIC_LIBRARY)
