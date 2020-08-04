//package saar.server;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
//import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.task.AsyncTaskExecutor;
//import org.springframework.scheduling.annotation.AsyncConfigurer;
//import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//import org.springframework.web.context.request.NativeWebRequest;
//import org.springframework.web.context.request.WebRequest;
//import org.springframework.web.context.request.async.CallableProcessingInterceptor;
//import org.springframework.web.context.request.async.TimeoutCallableProcessingInterceptor;
//import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//import org.springframework.web.servlet.mvc.method.annotation.CallableMethodReturnValueHandler;
//
//import java.lang.reflect.Method;
//import java.util.concurrent.Callable;
//
//@Configuration
//@EnableAsync
//@EnableScheduling
//public class AsyncConfiguration implements AsyncConfigurer {
//
//    private final Logger log = LoggerFactory.getLogger(AsyncConfiguration.class);
//
//    @Override
//    @Bean (name = "taskExecutor")
//    public AsyncTaskExecutor getAsyncExecutor() {
//        log.debug("Creating Async Task Executor");
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(5);
//        executor.setMaxPoolSize(10);
//        executor.setQueueCapacity(25);
//        return executor;
//    }
//
//    @Override
//    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
//        return new SimpleAsyncUncaughtExceptionHandler();
//    }
//
//    /** Configure async support for Spring MVC. */
//    @Bean
//    public WebMvcConfigurer webMvcConfigurerConfigurer(AsyncTaskExecutor taskExecutor, CallableProcessingInterceptor callableProcessingInterceptor) {
//        return new WebMvcConfigurer() {
//            @Override
//            public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
//                configurer.setDefaultTimeout(360000).setTaskExecutor(taskExecutor);
//                configurer.registerCallableInterceptors(callableProcessingInterceptor);
//                WebMvcConfigurer.super.configureAsyncSupport(configurer);
//            }
//        };
//    }
//
//    @Bean
//    public CallableProcessingInterceptor callableProcessingInterceptor() {
//        return new TimeoutCallableProcessingInterceptor() {
//            @Override
//            public <T> void beforeConcurrentHandling(NativeWebRequest request, Callable<T> task) throws Exception {
//                log.error("beforeConcurrentHandling!");
//                super.beforeConcurrentHandling(request, task);
//            }
//
//            @Override
//            public <T> void preProcess(NativeWebRequest request, Callable<T> task) throws Exception {
//                log.error("preProcess!");
//                super.preProcess(request, task);
//            }
//
//            @Override
//            public <T> void postProcess(NativeWebRequest request, Callable<T> task, Object concurrentResult) throws Exception {
//                log.error("postProcess!");
////                super.postProcess(request, task, concurrentResult);
//
//                if(concurrentResult instanceof RuntimeException){
//                    System.out.println("runtimeexception");
//                    Object o = super.handleTimeout(request, task);
//                }
//            }
//
//            @Override
//            public <T> void afterCompletion(NativeWebRequest request, Callable<T> task) throws Exception {
//                log.error("Completed!");
//                super.afterCompletion(request, task);
//            }
//
//            @Override
//            public <T> Object handleTimeout(NativeWebRequest request, Callable<T> task) throws Exception {
//                log.error("timeout!");
//                return super.handleTimeout(request, task);
//            }
//
//            @Override
//            public <T> Object handleError(NativeWebRequest request, Callable<T> task, Throwable t) throws Exception {
//                log.error("timeout!");
//                WebRequest r;
//                return super.handleTimeout(request, task);
//            }
//        };
//    }
//}
