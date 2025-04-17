package ru.otus;


public class TestLogging implements LogInterface {

    @Log
    @Override
    public void calculation(int param) { }

    @Override
    @Log
    public void calculation(int param1, int param2) { }

    @Override
    @Log
    public void calculation(int param1, int param2, String param3) { }

}
