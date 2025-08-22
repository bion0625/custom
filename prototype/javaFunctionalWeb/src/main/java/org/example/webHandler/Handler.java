package org.example.webHandler;

import org.example.dto.Request;
import org.example.dto.Response;

@FunctionalInterface
public interface Handler {
    Response handle(Request request) throws Exception;
}
