package com.mikerusoft.cassandra.pageslices;

import com.mikerusoft.cassandra.pageslices.flow.ApplicationFlowManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.Disposable;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
@Slf4j
public class PageSlicesApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(PageSlicesApplication.class, args);
    }

    @Autowired
    private ApplicationFlowManager flowManager;

    @Override
    public void run(String... args) throws Exception {

        CountDownLatch latch = new CountDownLatch(1);

        Disposable disposable = flowManager.store().doOnComplete(latch::countDown)
                .doOnError(t -> log.error("", t)).subscribe();

        latch.await();
        if (!disposable.isDisposed())
            disposable.dispose();

    }
}
