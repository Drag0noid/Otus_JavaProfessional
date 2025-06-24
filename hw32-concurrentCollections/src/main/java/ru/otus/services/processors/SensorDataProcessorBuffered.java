package ru.otus.services.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.api.SensorDataProcessor;
import ru.otus.api.model.SensorData;
import ru.otus.lib.SensorDataBufferedWriter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SensorDataProcessorBuffered implements SensorDataProcessor {
    private static final Logger log = LoggerFactory.getLogger(SensorDataProcessorBuffered.class);

    private final int bufferSize;
    private final SensorDataBufferedWriter writer;
    private final List<SensorData> dataBuffer;

    public SensorDataProcessorBuffered(int bufferSize, SensorDataBufferedWriter writer) {
        this.bufferSize = bufferSize;
        this.writer = writer;
        this.dataBuffer = new ArrayList<>(bufferSize);
    }

    @Override
    public void process(SensorData data) {
        synchronized (this) {
            dataBuffer.add(data);
            if (dataBuffer.size() >= bufferSize) {
                flush();
            }
        }
    }

    public synchronized void flush() {
        if (dataBuffer.isEmpty()) {
            return;
        }
        try {
            List<SensorData> dataToFlush = new ArrayList<>(dataBuffer);
            dataToFlush.sort(Comparator.comparing(SensorData::getMeasurementTime));
            writer.writeBufferedData(dataToFlush);
        } catch (Exception e) {
            log.error("Ошибка в процессе записи буфера", e);
        } finally {
            dataBuffer.clear();
        }
    }

    @Override
    public void onProcessingEnd() {
        flush();
    }
}
