package ru.otus.processor;

import org.junit.jupiter.api.Test;
import ru.otus.model.Message;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("squid:S1611")
class ProcessorSwapFieldsTest {

    @Test
    void test1() {
        Message originalMessage = new Message.Builder(1L).field11("value11").field12("value12").build();

        Processor processor = new ProcessorSwapFields();

        Message result = processor.process(originalMessage);

        assertEquals("value12", result.getField11());
        assertEquals("value11", result.getField12());

        System.out.println("Successful!");
    }

    @Test
    void test2() {
        Message originalMessage = new Message.Builder(2L).field11(null).field12("notNull").build();

        Processor processor = new ProcessorSwapFields();

        Message result = processor.process(originalMessage);

        assertEquals("notNull", result.getField11());
        assertEquals(null, result.getField12());

        System.out.println("Successful!");
    }
}
