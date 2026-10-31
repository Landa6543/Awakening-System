package com.soybe.awakeningsystem.yungsapi.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public class JSON {
    private JSON() {}

    public static Gson gson = newGsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static String toJsonString(Object object) {
        return gson.toJson(object);
    }

    public static void createJsonFileFromObject(Path path, Object object) throws IOException {
        String jsonString = gson.toJson(object);
        Files.write(path, jsonString.getBytes());
    }

    public static <T> T loadObjectFromJsonFile(Path path, Class<T> objectClass, Gson customGson) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            return customGson.fromJson(reader, objectClass);
        }
    }

    public static GsonBuilder newGsonBuilder() {
        return new GsonBuilder();
    }
}
