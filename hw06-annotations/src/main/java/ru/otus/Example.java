package ru.otus;

public class Example {

    @Before
    public void beforeFirst() {

    }

    @Before
    public void beforeSecond() {

    }

    @Test
    public void testFirst() {
        throw new RuntimeException("example");
    }

    @Test
    public void testSecond() {

    }

    @Test
    public void testThird() {

    }

    @Test
    public void testFourght() {
        String str = null;
        str.length();
    }

    @After
    public void afterFirst() {

    }

    @After
    public void afterSecond() {

    }
}
