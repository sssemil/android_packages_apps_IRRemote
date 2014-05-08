ifeq ($(BOARD_HAVE_IR_BLASTER),true)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := IRRemote

LOCAL_REQUIRED_MODULES := libjni_sonyopenir
LOCAL_JNI_SHARED_LIBRARIES := libjni_sonyopenir

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
endif
