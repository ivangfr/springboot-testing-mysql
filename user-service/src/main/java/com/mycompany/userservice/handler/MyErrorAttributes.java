package com.mycompany.userservice.handler;

import com.mycompany.userservice.exception.UserDataDuplicatedException;
import com.mycompany.userservice.exception.UserNotFoundException;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@Component
public class MyErrorAttributes extends DefaultErrorAttributes {

    public MyErrorAttributes() {
        super(true);
    }

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, includeStackTrace);

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
        private static Map<String, String> map = new HashMap<>();

        static {
            map.put(UserNotFoundException.class.getName(), ErrorCode.USER_NOT_FOUND.getDescription());
            map.put(UserDataDuplicatedException.class.getName(), ErrorCode.USER_DATA_DUPLICATED.getDescription());
        }

        static String getErrorCode(String className) {
            return map.get(className);
        }
    }

    private enum ErrorCode {
        USER_NOT_FOUND("UserNotFound"),
        USER_DATA_DUPLICATED("UserDataDuplicated");

        private final String description;

        ErrorCode(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

}
