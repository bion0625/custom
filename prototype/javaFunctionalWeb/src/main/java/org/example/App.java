package org.example;

import io.undertow.Undertow;
import org.example.webHandler.Handler;
import org.example.webHandler.HandlerBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class App {
    public static void main(String[] args) {
        Map<String, Handler> routes = new HashMap<>();
        routes.put("GET /hello", HandlerBuilder.helloHandler);
        routes.put("POST /echo", HandlerBuilder.echoHandler);

        Handler app =
                HandlerBuilder.chain(
                        HandlerBuilder.router(routes),
                        HandlerBuilder.requireApiKey("secret123"),
                        HandlerBuilder.validate(req ->
                                (req.method().equals("GET") && req.path().equals("/hello") &&
                                !req.query().containsKey("name"))
                                ? Optional.of("missing 'name'") : Optional.empty()
                                ),
                        HandlerBuilder.logging()
                );

        Undertow.builder()
                .addHttpListener(8080, "0.0.0.0")
                .setHandler(HandlerBuilder.undertowAdapter(app))
                .build()
                .start();
    }
}