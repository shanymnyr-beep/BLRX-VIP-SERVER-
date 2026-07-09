LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := runner

# Automatically include every .cpp file placed in this jni folder
MY_CPP := $(wildcard $(LOCAL_PATH)/*.cpp)
LOCAL_SRC_FILES := $(MY_CPP:$(LOCAL_PATH)/%=%)

LOCAL_CPPFLAGS := -std=c++11 -fexceptions
LOCAL_LDLIBS := -llog -landroid

include $(BUILD_SHARED_LIBRARY)
