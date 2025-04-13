package ru.otus.dataprocessor;

import java.util.*;
import java.util.stream.Collectors;

import ru.otus.model.Measurement;

public class ProcessorAggregator implements Processor {

    @Override
    public Map<String, Double> process(List<Measurement> data) {
        return data.stream()
                .collect(Collectors.groupingBy(
                        Measurement::name, TreeMap::new, Collectors.summingDouble(Measurement::value)));
    }
}