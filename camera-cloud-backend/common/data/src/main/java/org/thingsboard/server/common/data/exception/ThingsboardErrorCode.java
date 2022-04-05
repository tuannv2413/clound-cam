/**
 * Copyright © 2016-2021 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.common.data.exception;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ThingsboardErrorCode {

    GENERAL(2), // Lỗi xử lý trên server
    AUTHENTICATION(10), //Lỗi xác thực
    JWT_TOKEN_EXPIRED(11), //Token hết hạn
    CREDENTIALS_EXPIRED(15), //Quá thời gian xác thực
    PERMISSION_DENIED(20), //Từ chố truy cập vào api
    INVALID_ARGUMENTS(30), //Body không hợp lệ
    BAD_REQUEST_PARAMS(31), //Param header không hợp lệ
    ITEM_NOT_FOUND(32), //Không tìm thấy item
    TOO_MANY_REQUESTS(33), //Quá nhiều request
    TOO_MANY_UPDATES(34), // Quá nhiều update
    SUBSCRIPTION_VIOLATION(40), //Lỗi kết nối (web socket)
    NEWPASSWORD_SAME_OLDPASSWROD(41), // Lỗi  new password trùng old password
    USER_NOT_ACTIVE(42); // lỗi tài khoản user bị khóa

    private int errorCode;

    ThingsboardErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    @JsonValue
    public int getErrorCode() {
        return errorCode;
    }

}
