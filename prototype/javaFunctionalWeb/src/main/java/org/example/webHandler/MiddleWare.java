package org.example.webHandler;

@FunctionalInterface
public interface MiddleWare {
    Handler apply(Handler next);
}
