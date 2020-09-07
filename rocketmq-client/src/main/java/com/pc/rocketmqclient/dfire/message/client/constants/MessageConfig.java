//package com.pc.rocketmqclient.dfiremessage.client.constants;
//
//import org.apache.commons.lang3.StringUtils;
//
///**
// * @author gantang
// * @Date 2017/5/22
// */
//public class MessageConfig {
//    private static String env;
//
//    /**
//     * 设置环境
//     *
//     * @param env
//     */
//    public void setEnv(String env) {
//        MessageConfig.env = env;
//    }
//
//    public void setSuspend(boolean suspend) {
//        if (checkUnPublishEnv()) {
//            ConsumerListenerForRm.suspend = suspend;
//        }
//    }
//
//    public static boolean checkUnPublishEnv() {
//        return StringUtils.containsAny(env, "dev", "daily", "pre");
//    }
//}
