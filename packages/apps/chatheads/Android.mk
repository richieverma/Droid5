LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_SDK_VERSION := current

LOCAL_STATIC_JAVA_LIBRARIES := cr android-support-v4

LOCAL_PACKAGE_NAME := chatheads

include $(BUILD_PACKAGE)
##################################################
include $(CLEAR_VARS)

include $(BUILD_MULTI_PREBUILT)

# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
