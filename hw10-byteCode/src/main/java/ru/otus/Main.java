package ru.otus;

public class Main {
    public static void main(String[] args) {
        var original = new TestLogging();

        var proxy = ProxyFactory.createProxy(original, LogInterface.class);
        
        proxy.calculation(1);
        proxy.calculation(1, 2);
        proxy.calculation(1, 2, "3");
    }

}