package com.roundesk.sdk.util

open class URLConfigurationUtil {

    companion object {
        fun getBaseURL(): String {
//            var baseurl = ""
//            when (Constants.BUILD_TYPE) {
//                Constants.DEV -> {
//                    baseurl = Constants.BASE_URL_DEV
//                }
//                Constants.UAT -> {
//                    baseurl = Constants.BASE_URL_UAT
//                }
//                Constants.PRODUCTION -> {
//                    baseurl = Constants.BASE_URL_PRODUCTION
//                }
//            }
            return Constants.BASE_URL_DEV
        }

        fun getSocketURL(): String {
            var socketurl = ""
//            when (Constants.BUILD_TYPE) {
//                Constants.DEV -> {
//                    socketurl = Constants.SOCKET_URL_DEV
//                }
//                Constants.UAT -> {
//                    socketurl = Constants.SOCKET_URL_UAT
//                }
//                Constants.PRODUCTION -> {
//                    socketurl = Constants.SOCKET_URL_PRODUCTION
//                }
//            }
            return Constants.SOCKET_URL_DEV
        }

        fun getServerURL(): String {
//            var serverurl = ""
//            when (Constants.BUILD_TYPE) {
//                Constants.DEV -> {
//                    serverurl = Constants.SERVER_URL_DEV
//                }
//                Constants.UAT -> {
//                    serverurl = Constants.SERVER_URL_UAT
//                }
//                Constants.PRODUCTION -> {
//                    serverurl = Constants.SERVER_URL_PRODUCTION
//                }
//            }
            return Constants.SERVER_URL_DEV
        }
    }
}