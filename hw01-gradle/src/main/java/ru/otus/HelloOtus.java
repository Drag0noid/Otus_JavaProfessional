package ru.otus;

import com.google.common.base.Joiner;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("java:S106")
public class HelloOtus {
    public static void main(String... args) {
        List<String> words = Arrays.asList("Hello", "Otus!");
        String result = Joiner.on(", ").join(words);

        System.out.println(result);
    }
}