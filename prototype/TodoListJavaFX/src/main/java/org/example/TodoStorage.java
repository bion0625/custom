package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TodoStorage {
    private static final String FILE_PATH = "todo.json";
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public static void save(List<ToDoItem> items) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), items);
    }

    public static List<ToDoItem> load() throws IOException {
        File file = new File(FILE_PATH);
        if (!file.exists()) return List.of();
        return mapper.readValue(file, new TypeReference<>() {});
    }
}
