package org.example.webHandler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.example.dto.Request;
import org.example.dto.Response;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

public class HandlerBuilder {
    public static Handler chain(Handler terminal, MiddleWare... mws) {
        Handler h = terminal;
        for (int i = mws.length - 1; i >= 0; i--)
            h = mws[i].apply(h);
        return h;
    }

    public static MiddleWare logging() {
        return next -> req -> {
            System.out.printf("[LOG] %s %s%n", req.method(), req.path());
            return next.handle(req);
        };
    }

    public static MiddleWare requireApiKey(String expected) {
        return next -> req -> {
            if (!Objects.equals(req.headers().getOrDefault("x-api-key", ""), expected)) {
                return Response.of(401, "Unauthorized");
            }
            return next.handle(req);
        };
    }

    public static MiddleWare validate(Function<Request, Optional<String>> validator) {
        return next -> req -> {
            var err = validator.apply(req);
            if (err.isPresent()) return Response.of(400, err.get());
            return next.handle(req);
        };
    }

    public static Handler router(Map<String, Handler> routes) {
        return req -> {
            Handler h = routes.get(req.method() + " " + req.path());
            if (h == null) return Response.of(404, "Not Found");
            return h.handle(req);
        };
    }

    public static Handler helloHandler = req -> {
        String name = req.query().getOrDefault("name", List.of("world")).get(0);
        String body = "{\"message\":\"Hello, " + name.replace("\"","\\\"") + "\"}";
        return new Response(200, Map.of("Content-Type","application/json; charset=utf-8"), body);
    };

    public static Handler echoHandler = req -> new Response(
            200,
            Map.of("Content-Type","application/json; charset=utf-8"),
            String.format("""
                    {"you_sent" : %s}
                    """, req.body())
    );

    public static HttpHandler undertowAdapter(Handler app) {
        // 람다 대신 익명 클래스 사용: dispatch 시 this 재사용 쉬움
        return new HttpHandler() {
            @Override
            public void handleRequest(HttpServerExchange exchange) throws Exception {
                if (exchange.isInIoThread()) {
                    // IO 스레드면 워커로 보내고 즉시 리턴
                    exchange.dispatch(this);
                    return;
                }

                // 이제 워커 스레드! 블로킹 I/O 가능
                exchange.startBlocking();

                Request req = toRequest(exchange); // 여기서 readAllBytes 사용 OK
                Response res;
                try {
                    res = app.handle(req);
                } catch (Exception e) {
                    res = Response.of(500, "Internal Server Error");
                }
                write(exchange, res);
            }
        };
    }

    public static Request toRequest(HttpServerExchange ex) throws Exception {
        ex.startBlocking();
        String body = new String(ex.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, List<String>> query = new HashMap<>();
        ex.getQueryParameters().forEach((k, v) -> query.put(k, new ArrayList<>(v)));
        Map<String, String> headers = new HashMap<>();
        ex.getRequestHeaders().forEach(h -> headers.put(h.getHeaderName().toString(), h.getFirst()));
        return new Request(ex.getRequestMethod().toString(),
                ex.getRequestPath(), query, headers, body);
    }

    public static void write(HttpServerExchange ex, Response res) {
        ex.setStatusCode(res.status());
        res.headers().forEach((k, v) -> ex.getResponseHeaders().put(Headers.fromCache(k), v));
        ex.getResponseSender().send(res.body());
    }
}
