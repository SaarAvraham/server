package saar.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
public class Controller {
    private final Logger logger = LoggerFactory.getLogger(Controller.class);

    private static int counter = 1;

    @GetMapping("/counter")
    public @ResponseBody
    int getCounter(final HttpServletResponse response) throws IOException {
        response.getWriter().close();
        return counter++;
    }

    @GetMapping("/srb")
    public ResponseEntity<StreamingResponseBody> handleRbe(final HttpServletResponse response, HttpServletRequest request) throws IOException, ServletException {
        response.setHeader(
                "Content-Disposition",
                "attachment;filename=sample.txt");
        AtomicBoolean success = new AtomicBoolean(true);
        response.setTrailerFields(() -> {
            HashMap<String, String> stringStringHashMap = new HashMap<>();
            stringStringHashMap.put("Success:", String.valueOf(success.get()));
            return stringStringHashMap;
        });
        StreamingResponseBody stream = out -> {
            for (int i = 0; i < 5000; i++) {
                String msg = i + " dsaflkasjlkadsjgijiotewjaiojteroiajgfmadfagnfkjnwaejnjtkjnwebvngngnfgfhhgftjanbgfdfbgdbhdfgfjknawejtnwaejndsgsdagfsdds "
                        + System.lineSeparator();



                if (i % 10 == 0) {
                    out.flush();
                }

                if (i == 2000) {
//                    response.sendError(503, "Error while streaming");
//                    response.reset()


//                    Thread.currentThread().interrupt();
//                    throw new org.springframework.web.context.request.async.AsyncRequestTimeoutException();

                }
                out.write(msg.getBytes());
            }
        };
        return new ResponseEntity(stream, HttpStatus.OK);
    }

    @GetMapping(value = "/download", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingResponseBody> download(final HttpServletResponse response) {
        response.setContentType("application/zip");
        response.setHeader(
                "Content-Disposition",
                "attachment;filename=sample.zip");
        StreamingResponseBody stream = out -> {
            final ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());
            try {
                final ZipEntry zipEntry = new ZipEntry("saar");
                zipOut.putNextEntry(zipEntry);

                for (int i = 0; i < 500; i++) {
                    String msg = counter++ +
                            " dsaflkasjlkadsjgijiotewjaiojteroiajgfmadfagnfkjnwaejnjtkjnwebvngngnfgfhhgftjanbgfdfbgdbhdfgfjknawejtnwaejndsgsdagfsdds "
                            + System.lineSeparator();
                    zipOut.write(msg.getBytes());

                    if (i % 10 == 0) {
                        zipOut.flush();
                    }

                    if (i == 300) {
                        throw new RuntimeException();
                    }
                }

                zipOut.close();
            } catch (final IOException e) {
                logger.error("Exception while reading and streaming data {} ", e);
            }
        };
        logger.info("steaming response {} ", stream);
        return new ResponseEntity(stream, HttpStatus.OK);
    }


    private ExecutorService nonBlockingService = Executors
            .newCachedThreadPool();

    @GetMapping(value = "/sse", produces = "application/json")
    public SseEmitter handleSse() {
        SseEmitter emitter = new SseEmitter();
        nonBlockingService.execute(() -> {
            try {
                for (int i = 0; i < 5000; i++) {
                    String msg = i + " dsaflkasjlkadsjgijiotewjaiojteroiajgfmadfagnfkjnwaejnjtkjnwebvngngnfgfhhgftjanbgfdfbgdbhdfgfjknawejtnwaejndsgsdagfsdds "
                            + System.lineSeparator();
                    emitter.send(msg, MediaType.parseMediaType("text/csv"));

                    if (i == 6) {
                        throw new Exception();
                    }
                }
                // we could send more events
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);

            }
        });
        return emitter;
    }

    @GetMapping("/servlet")
    public void hs(final HttpServletResponse response, HttpServletRequest request) throws IOException, ServletException {
        response.setHeader(
                "Content-Disposition",
                "attachment;filename=sample.txt");
        response.setStatus(200);

        ServletOutputStream out = response.getOutputStream();
        for (int i = 0; i < 5000; i++) {
            String msg = i + " dsaflkasjlkadsjgijiotewjaiojteroiajgfmadfagnfkjnwaejnjtkjnwebvngngnfgfhhgftjanbgfdfbgdbhdfgfjknawejtnwaejndsgsdagfsdds "
                    + System.lineSeparator();

            if (i % 10 == 0) {
                out.flush();
            }

            if (i == 2000) {
//                    response.sendError(503, "Error while streaming");
//                    response.reset()
                boolean connection = response.containsHeader("Connection");
                response.setHeader("connection", "close");
                String connection1 = response.getHeader("connection");

//                    Thread.currentThread().interrupt();
                response.getWriter().close();
                response.getOutputStream().close();
                ServletOutputStream outputStream = response.getOutputStream();
                outputStream.setWriteListener(new WriteListener() {
                    @Override
                    public void onWritePossible() throws IOException {

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        System.out.println();
                    }
                });
                response.sendError(500, "dfsfsd");
//                    throw new org.springframework.web.context.request.async.AsyncRequestTimeoutException();

            }
            out.write(msg.getBytes());
        }
        ;
    }
}
