package ru.otus.dataprocessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class FileSerializer implements Serializer {
    private final ObjectMapper objectMapper = JsonMapper.builder().build();
    private final String filePath;

    public FileSerializer(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void serialize(Map<String, Double> data) {
        try {
            objectMapper.writeValue(new File(filePath), new LinkedHashMap<>(data));
        } catch (IOException e) {
            throw new FileProcessException(e);
        }
    }
}