package saar.server;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
public class Controller {
    private final Logger logger = LoggerFactory.getLogger(Controller.class);

    private static int counter = 1;

    @EventListener(ApplicationReadyEvent.class)
    public void launch() throws Exception {
//        CompletableFuture<Command> commandCompletableFuture = openChannel.asyncCompletableRpc(null);
//        commandCompletableFuture.whenCompleteAsync((command, throwable) -> command.getContentBody(), executorService)
//        getTweetsNonBlocking(null);
//        readResultsAndInsertToDB();
    }

    @SneakyThrows
    public void readResultsAndInsertToDB() {
        try {

            RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
            BufferedReader bufferedReader = restTemplate.execute("http://localhost:9090/srb", HttpMethod.GET, null,
                    clientHttpResponse ->
                            new BufferedReader(new InputStreamReader(clientHttpResponse.getBody())));

            Gson gson = new Gson();
            JsonReader jsonReader = new JsonReader(bufferedReader);
            while (jsonReader.hasNext()) {
                jsonReader.beginObject();
                String name = jsonReader.nextName();
                jsonReader.beginArray();
                while (jsonReader.hasNext()) {
                    SearchResult searchResult = gson.fromJson(jsonReader, SearchResult.class);

                    System.out.println("\t" + searchResult);
                }
                jsonReader.endArray();
                String successName = jsonReader.nextName();
                String value = jsonReader.nextString();
                System.out.println(successName + ": " + value);
            }
            jsonReader.endObject();
            jsonReader.close();
            System.out.println("DONE!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping(value = "/tweets-non-blocking",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<SearchResult2> getTweetsNonBlocking(final HttpServletResponse response) {
        if (response != null) {
            response.setHeader(
                    "Content-Disposition",
                    "attachment;filename=sample.txt");
        }

        System.out.println("Starting NON-BLOCKING Controller!");
        Flux<SearchResult2> flux = WebClient.create()
                .get()
                .uri("http://localhost:9090/slow-service-tweets")
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    System.out.println("CLIENT GOT 5xx STATUS");
                    return Mono.error(new RuntimeException("SAAR"));
                })
                .bodyToFlux(SearchResult2.class)
                .doOnTerminate(() -> {
                    System.out.println("onTerminate");
                })
                .doOnError(throwable -> {
                    System.out.println("onError");
                })
                .doOnCancel(() -> {
                    System.out.println("onCancel");
                })
                .doOnNext(searchResult2 -> System.out.println("Client got:" + searchResult2));

        flux.subscribe();
//        flux.subscribe(searchResult -> System.out.println("Client got (from subscribe):" + searchResult));
        System.out.println("Exiting NON-BLOCKING Controller!");
        return flux;
    }
//
//    @GetMapping(path = "/slow-service-tweets", produces = "application/stream+json")
//    public Flux<SearchResult> getCarStream() {
//        Flux<SearchResult> searchResultFlux = Flux.fromStream(() -> {
//            AtomicInteger i = new AtomicInteger();
////            return getAllTweets().stream();
//            return Stream.generate(() -> {
//                SearchResult searchResult = new SearchResult(i.getAndIncrement(), "adas");
//                if (i.get() == 5) {
//                    return new SearchResult(-1, "SDA");
//                }
//
//                return searchResult;
//            });
//        });
//        return searchResultFlux;
//    }
//
//    @GetMapping("/counter")
//    public @ResponseBody
//    int getCounter(final HttpServletResponse response) throws IOException {
//        response.getWriter().close();
//        return counter++;
//    }

    //  http://localhost:9090/policies/policies/{policyId}/instances/{policyInstanceId}/results
    @GetMapping("/policies/{policyId}/instances/{policyInstanceId}/tasks/results")
    public ResponseEntity<StreamingResponseBody> handleResults(final HttpServletResponse response, @PathVariable String policyId, @PathVariable String policyInstanceId) {
        response.setHeader(
                "Content-Disposition",
                "attachment;filename=sample.txt");
        Map<PolicyIdentifier, List<SearchResult>> searchResultMap = new HashMap<>();
        Instant now = Instant.now();

        searchResultMap.put(new PolicyIdentifier(1, 2), Arrays.asList(new SearchResult(0, now.minusSeconds(60)),
                new SearchResult(1, now.minusSeconds(30)),
                new SearchResult(2, now.minusSeconds(20)),
                new SearchResult(3, now.minusSeconds(10)),
                new SearchResult(4, now.minusSeconds(5)),
                new SearchResult(5, now.minusSeconds(2)),
                new SearchResult(6, now.minusSeconds(1))));

        List<SearchResult> value = Arrays.asList(new SearchResult(0, now.minusSeconds(60)),
                new SearchResult(13, now.minusSeconds(30 * 60)),
                new SearchResult(23, now.minusSeconds(20 * 60)),
                new SearchResult(33, now.minusSeconds(10 * 60)),
                new SearchResult(63, now.minusSeconds(60)));
        searchResultMap.put(new PolicyIdentifier(1, 3), value);
        PolicyIdentifier policyIdentifier = new PolicyIdentifier(Integer.parseInt(policyId), Integer.parseInt(policyInstanceId));

        System.out.println("Got request for " + policyIdentifier);
        StreamingResponseBody stream = new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream out) throws IOException {
                int numOfSegments;
                JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
                writer.setIndent("  ");
                writer.beginObject();

                List<SearchResult> searchResults = searchResultMap.getOrDefault(policyIdentifier, new ArrayList<>());

//                if (policyIdentifier.getPolicyId() == 0 && policyIdentifier.getPolicyInstanceId() == 0) {
//                    numOfSegments = 1000000;
//                } else {
//                    numOfSegments = searchResults.size();
//                }

                numOfSegments = 1000000;

                System.out.println("Starting to stream " + numOfSegments + " segments");
                writer.name("numOfSegments").value(numOfSegments);
                writer.name("searchResults");
                writer.beginArray();

                for (int j = 0; j < numOfSegments; j++) {
                    SearchResult searchResult = new SearchResult(j, Instant.now().plusSeconds(j));
                    String row = searchResult.segmentId + "," + searchResult.contactStartTime.toString();
                    writer.value(row);
                    if (j % 10000 == 0)
                        System.out.println(j);

                    if (j % 10000 == 0) {
                        writer.flush();
                        out.flush();
                    }
                }

                writer.endArray();
                writer.name("status").value("Success");
                writer.endObject();
                writer.close();
            }
        };

        return new ResponseEntity(stream, HttpStatus.OK);
    }


//    //  http://localhost:9090/policies/policies/{policyId}/instances/{policyInstanceId}/results
//    @GetMapping("/policies/{policyId}/instances/{policyInstanceId}/tasks/results")
//    public ResponseEntity<StreamingResponseBody> handleResults(final HttpServletResponse response, @PathVariable String policyId, @PathVariable String policyInstanceId) {
//        response.setHeader(
//                "Content-Disposition",
//                "attachment;filename=sample.txt");
//        Map<PolicyIdentifier, List<SearchResult>> searchResultMap = new HashMap<>();
//        Instant now = Instant.now();
//
//        searchResultMap.put(new PolicyIdentifier(1, 2), Arrays.asList(new SearchResult(0, now.minusSeconds(60)),
//                new SearchResult(1, now.minusSeconds(30)),
//                new SearchResult(2, now.minusSeconds(20)),
//                new SearchResult(3, now.minusSeconds(10)),
//                new SearchResult(4, now.minusSeconds(5)),
//                new SearchResult(5, now.minusSeconds(2)),
//                new SearchResult(6, now.minusSeconds(1))));
//
//        List<SearchResult> value = Arrays.asList(new SearchResult(0, now.minusSeconds(60)),
//                new SearchResult(13, now.minusSeconds(30 * 60)),
//                new SearchResult(23, now.minusSeconds(20 * 60)),
//                new SearchResult(33, now.minusSeconds(10 * 60)),
//                new SearchResult(63, now.minusSeconds(60)));
//        searchResultMap.put(new PolicyIdentifier(1, 3), value);
//        PolicyIdentifier policyIdentifier = new PolicyIdentifier(Integer.parseInt(policyId), Integer.parseInt(policyInstanceId));
//
//        System.out.println("Got request for " + policyIdentifier);
//        StreamingResponseBody stream = new StreamingResponseBody() {
//            @Override
//            public void writeTo(OutputStream out) throws IOException {
//                JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
//                writer.setIndent("  ");
//                writer.beginObject();
//
//                List<SearchResult> searchResults = searchResultMap.getOrDefault(policyIdentifier, new ArrayList<>());
//
//                if(policyIdentifier.getPolicyId() == 0 && policyIdentifier.getPolicyInstanceId() == 0){
//                    for (int i = 0; i < 65000; i++) {
//                        searchResults.add(new SearchResult(i, Instant.now().plusSeconds(i)));
//                    }
//                }
//
//                int numOfSegments = searchResults.size();
//                System.out.println("Starting to stream "+ numOfSegments + " segments");
//
//                writer.name("numOfSegments").value(numOfSegments);
//                writer.name("searchResults");
//                writer.beginArray();
//
//                int i = 0;
//
//                for (SearchResult searchResult : searchResults) {
//                    String row = searchResult.segmentId + "," + searchResult.contactStartTime.toString();
//                    writer.value(row);
//                    System.out.println(row);
//
//                    if(policyIdentifier.getPolicyId() == 0 && policyIdentifier.getPolicyInstanceId() == 0){
//                        for (int i = 0; i < 65000; i++) {
//                            searchResults.add(new SearchResult(i, Instant.now().plusSeconds(i)));
//                        }
//                    }
//
//                    if (i++ % 10 == 0) {
//                        writer.flush();
//                        out.flush();
//                    }
//                }
//
//                writer.endArray();
//                writer.name("status").value("Success");
//                writer.endObject();
//                writer.close();
//            }
//        };
//
//        return new ResponseEntity(stream, HttpStatus.OK);
//    }
//    public void writeJsonStream(OutputStream out, boolean json) throws IOException, InterruptedException {
//        Gson gson = new Gson();
//        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
//        writer.setIndent("  ");
//        writer.beginObject();
//        writer.name("searchResults");
//        writer.beginArray();
//
//        for (int i = 0; i < 1000000; i++) {
//            SearchResult searchResult = new SearchResult(i, "amir");
//
//            if (json) {
//                gson.toJson(searchResult, SearchResult.class, writer);
//            } else {
//                writer.value(i + "dsa, dsadsa, dsawdsa");
//            }
//            if (i % 10 == 0) {
//                writer.flush();
//                out.flush();
//            }
//        }
//
//        writer.endArray();
//        writer.name("status").value("Success");
//        writer.endObject();
//        writer.close();
//    }

//    @GetMapping("/srb")
//    public ResponseEntity<StreamingResponseBody> handleRbe(final HttpServletResponse response, HttpServletRequest request) throws IOException, ServletException {
//        response.setHeader(
//                "Content-Disposition",
//                "attachment;filename=sample.txt");
//        System.out.println("Got request");
//        StreamingResponseBody stream = out -> {
//            try {
//                writeJsonStream(out, false);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        };
//        return new ResponseEntity(stream, HttpStatus.OK);
//    }

//    @GetMapping("/slow-service-tweets")
//    private List<SearchResult> getAllTweets() {
//        return Arrays.asList(
//                new SearchResult(0),
//                new SearchResult(1),
//                new SearchResult(2));
//    }

//    private List<SearchResult> getAllTweets() {
//        return Arrays.asList(
//                new SearchResult(0, "adas"),
//                new SearchResult(1, "adas"),
//                new SearchResult(2, "adas"));
//    }

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
