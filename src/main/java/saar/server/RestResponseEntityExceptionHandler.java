package saar.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

//@ControllerAdvice
//public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
//
//    @Autowired
//    CallableProcessingInterceptor callableProcessingInterceptor;
//
//    @ExceptionHandler(value = { Exception.class})
//    protected ResponseEntity<Object> handleConflict(RuntimeException ex, NativeWebRequest request) throws Exception {
//
////        Thread.currentThread().interrupt();
////        callableProcessingInterceptor.handleTimeout(request, null);
//        return null;
//    }
//}