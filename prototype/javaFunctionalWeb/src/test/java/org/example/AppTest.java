package org.example;

import org.example.dto.Request;
import org.example.webHandler.Handler;
import org.example.webHandler.HandlerBuilder;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

class AppTest {
    @Test
    void hello_ok() throws Exception {
        var req = new Request(
                "GET", "/hello",
                Map.of("name", List.of("Tester")),
                Map.of("x-api-key","secret123"),
                ""
        );
        var res = HandlerBuilder.helloHandler.handle(req);
        assertEquals(200, res.status());
        assertTrue(res.body().contains("Tester"));
    }

    @Test
    void auth_fail() throws Exception {
        var routes = Map.of("GET /hello", HandlerBuilder.helloHandler);
        var app = HandlerBuilder.chain(HandlerBuilder.router(routes), HandlerBuilder.requireApiKey("secret123"));

        var req = new Request(
                "GET", "/hello",
                Map.of("name", List.of("A")),
                Map.of(), // no key
                ""
        );
        var res = app.handle(req);
        assertEquals(401, res.status());
    }
}