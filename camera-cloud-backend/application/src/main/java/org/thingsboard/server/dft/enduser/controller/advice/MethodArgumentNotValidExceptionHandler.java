package org.thingsboard.server.dft.enduser.controller.advice;

import lombok.Data;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.dft.exception.DuplicateFieldException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class MethodArgumentNotValidExceptionHandler {

  @ResponseStatus(BAD_REQUEST)
  @ResponseBody
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public Error methodArgumentNotValidException(MethodArgumentNotValidException ex) {
    BindingResult result = ex.getBindingResult();
    List<org.springframework.validation.FieldError> fieldErrors = result.getFieldErrors();
    return processFieldErrors(fieldErrors);
  }

  @ResponseStatus(BAD_REQUEST)
  @ResponseBody
  @ExceptionHandler(DuplicateFieldException.class)
  public Error DuplicateFieldException(DuplicateFieldException ex) {
    Map<String, String> mapError = new HashMap<>();
    mapError.put("duplicate_field", ex.getMessage());
    return new Error(BAD_REQUEST.value(), mapError, ThingsboardErrorCode.INVALID_ARGUMENTS, new Date());
  }

  private Error processFieldErrors(List<org.springframework.validation.FieldError> fieldErrors) {
    Error error = new Error(BAD_REQUEST.value(), new HashMap<>(), ThingsboardErrorCode.INVALID_ARGUMENTS, new Date());
    for (org.springframework.validation.FieldError fieldError : fieldErrors) {
      error.addMessageError(fieldError.getField(), fieldError.getDefaultMessage());
    }
    return error;
  }

  @Data
  static class Error {
    private final int status;
    private final Map<String, String> message;
    private final ThingsboardErrorCode errorCode;
    private final Date timestamp;

    Error(int status, Map<String, String> message, ThingsboardErrorCode errorCode, Date timestamp) {
      this.status = status;
      this.message = message;
      this.errorCode = errorCode;
      this.timestamp = timestamp;
    }

    public void addMessageError(String fieldError, String messageError) {
      message.put(fieldError, messageError);
    }

  }
}