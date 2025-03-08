package ru.calculator;

/*
-Xms256m
-Xmx256m
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=./logs/heapdump.hprof
-XX:+UseG1GC
-Xlog:gc=debug:file=./logs/gc-%p-%t.log:tags,uptime,time,level:filecount=5,filesize=10m
*/

/*              |   до оптимизации           |   после оптимизации
   память       |   время выполнения         |   время выполнения         |
----------------|----------------------------|----------------------------|
   256          | spend msec:467094, sec:467 | spend msec:419182, sec:419 |
   512          | spend msec:461321, sec:461 | spend msec:414568, sec:414 |
   1024         | spend msec:447464, sec:447 | spend msec:425632, sec:425 |
   1280         | spend msec:444891, sec:444 | spend msec:422429, sec:422 |
   1536         | spend msec:446792, sec:446 | spend msec:413791, sec:413 |
   2048         | spend msec:447151, sec:447 | spend msec:420442, sec:420 |

   Вывод: До оптимизации программы оптимальный размер хипа составлял 1280MB, дальнейшее его увеличение не повлияло на производительность
          После оптимизации программы оптимальный размер хипа составлял 1536MB
*/

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalcDemo {
    private static final Logger log = LoggerFactory.getLogger(CalcDemo.class);

    public static void main(String[] args) {
        long counter = 500_000_000;
        Summator summator = new Summator();
        long startTime = System.currentTimeMillis();

        for (int idx = 0; idx < counter; idx++) {
            Data data = new Data(idx);
            summator.calc(data);

            if (idx % 10_000_000 == 0) {
                log.info("{} current idx:{}", LocalDateTime.now(), idx);
            }
        }

        long delta = System.currentTimeMillis() - startTime;
        log.info("PrevValue:{}", summator.getPrevValue());
        log.info("PrevPrevValue:{}", summator.getPrevPrevValue());
        log.info("SumLastThreeValues:{}", summator.getSumLastThreeValues());
        log.info("SomeValue:{}", summator.getSomeValue());
        log.info("Sum:{}", summator.getSum());
        log.info("spend msec:{}, sec:{}", delta, (delta / 1000));
    }
}
