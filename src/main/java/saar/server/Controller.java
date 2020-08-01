package saar.server;

import org.apache.catalina.connector.ResponseFacade;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.ServletException;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
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

        response.setTrailerFields(new Supplier<Map<String, String>>() {
            @Override
            public Map<String, String> get() {
                HashMap<String, String> trailerFields = new HashMap<>();
                trailerFields.put("Connection", "close");
                return trailerFields;
            }
        });


//        response.getOutputStream().close();

        StreamingResponseBody stream = out -> {
            for (int i = 0; i < 5000; i++) {
                String msg = i + " dsaflkasjlkadsjgijiotewjaiojteroiajgfmadfagnfkjnwaejnjtkjnwebvngngnfgfhhgftjanbgfdfbgdbhdfgfjknawejtnwaejndsgsdagfsdds "
                        + System.lineSeparator();
                if (i == 2000) {
//                    response.sendError(503, "Error while streaming");
//                    response.reset()
                    boolean connection = response.containsHeader("Connection");
                    response.setHeader("connection", "close");
                    String connection1 = response.getHeader("connection");

//                    Thread.currentThread().interrupt();
                    response.getWriter().close();
                    response.getOutputStream().close();
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



                    if(i == 300){
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
}
