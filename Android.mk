ifeq ($(BOARD_HAVE_IR_BLASTER),true)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := IRRemote

LOCAL_STATIC_JAVA_LIBRARIES := \
        android-support-v4 \
        libGoogleAnalyticsServices

LOCAL_REQUIRED_MODULES := libjni_sonyopenir
LOCAL_JNI_SHARED_LIBRARIES := libjni_sonyopenir

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libGoogleAnalyticsServices:libs/libGoogleAnalyticsServices.jar

include $(BUILD_MULTI_PREBUILT)

include $(call all-makefiles-under,$(LOCAL_PATH))
endif
