package com.ivanfranchin.userservice.config;

import com.ivanfranchin.userservice.user.exception.UserDataDuplicatedException;
import com.ivanfranchin.userservice.user.exception.UserNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.error.ErrorAttributeOptions.Include;
import org.springframework.boot.webmvc.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@Component
public class ErrorAttributesConfig extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions errorAttributeOptions) {
        Throwable error = getError(webRequest);
        String exceptionClassName = error != null ? error.getClass().getName() : null;
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest,
                errorAttributeOptions.including(Include.MESSAGE, Include.BINDING_ERRORS));
        String errorCode = ErrorCodeHandler.getErrorCode(exceptionClassName);
        if (errorCode == null) {
            String statusError = (String) errorAttributes.get("error");
            errorCode = statusError.replaceAll("\\s+", "");
        }
        errorAttributes.put("errorCode", errorCode);
        return errorAttributes;
    }

    private static class ErrorCodeHandler {
        private static final Map<String, String> ERROR_CODE_MAP = new HashMap<>();

        static {
            ERROR_CODE_MAP.put(UserNotFoundException.class.getName(), ErrorCode.USER_NOT_FOUND.getDescription());
            ERROR_CODE_MAP.put(UserDataDuplicatedException.class.getName(), ErrorCode.USER_DATA_DUPLICATED.getDescription());
        }

        static String getErrorCode(String className) {
            return ERROR_CODE_MAP.get(className);
        }
    }

    @Getter
    @AllArgsConstructor
    private enum ErrorCode {
        USER_NOT_FOUND("UserNotFound"),
        USER_DATA_DUPLICATED("UserDataDuplicated");

        private final String description;
    }
}
