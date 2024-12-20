package com.ivanfranchin.userservice.config;

import com.ivanfranchin.userservice.user.exception.UserNotFoundException;
import com.ivanfranchin.userservice.user.exception.UserDataDuplicatedException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.error.ErrorAttributeOptions.Include;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@Component
public class MyErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions errorAttributeOptions) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest,
                errorAttributeOptions.including(Include.EXCEPTION, Include.MESSAGE, Include.BINDING_ERRORS));
        String exceptionClassName = (String) errorAttributes.get("exception");
        String errorCode = ErrorCodeHandler.getErrorCode(exceptionClassName);
        if (errorCode == null) {
            String statusError = (String) errorAttributes.get("error");
            errorCode = statusError.replaceAll("\\s+", "");
        }
        errorAttributes.put("errorCode", errorCode);
        return errorAttributes;
    }

    private static class ErrorCodeHandler {
        private static final Map<String, String> map = new HashMap<>();

        static {
            map.put(UserNotFoundException.class.getName(), ErrorCode.USER_NOT_FOUND.getDescription());
            map.put(UserDataDuplicatedException.class.getName(), ErrorCode.USER_DATA_DUPLICATED.getDescription());
        }

        static String getErrorCode(String className) {
            return map.get(className);
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
