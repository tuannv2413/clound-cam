package org.thingsboard.server.dft.util.constant;

public class ClAlarmConstant {
    // ClAlarmType
    public static final String TYPE_MOVING = "MOVING";
    public static final String TYPE_CONNECT = "CONNECT";
    public static final String TYPE_DISCONNECT = "DISCONNECT";

    // ClAlarmAction
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";

    // Day of week
    public static final String T2 = "T2";
    public static final String T3 = "T3";
    public static final String T4 = "T4";
    public static final String T5 = "T5";
    public static final String T6 = "T6";
    public static final String T7 = "T7";
    public static final String CN = "CN";

    // warning mail
    public static final String WARNING_EMAIL_SUBJECT = "Hệ thống Cloud Camera - Thiết bị {0}"; // 0: action
    public static final String WARNING_EMAIL_MESSAGE =
            "Hệ thống Cloud Camera xin thông báo: {0} {1} vào lúc: {2}</br> " + // 0: ten tb, 1: action, 2: alarmTime
            "Email này được gửi theo cài đặt nhận thông báo từ hệ thống Cloud Camera." +
                    " Trong trường hợp bạn không muốn nhận cảnh báo thông qua email, " +
                    "vui lòng liên hệ với Quản trị viên để thiết lập lại cài đặt";
    public static final String WARNING_NOTIFY_TITLE = "{0} {1}"; // 0: ten tb; 1: action
    public static final String WARNING_NOTIFY_BODY = "{0}"; // 0: alarmTime
}
