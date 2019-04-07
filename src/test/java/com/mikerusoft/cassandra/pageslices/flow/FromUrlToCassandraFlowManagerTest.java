package com.mikerusoft.cassandra.pageslices.flow;

import com.datastax.driver.core.Cluster;
import com.github.nosan.embedded.cassandra.spring.EmbeddedCassandra;
import com.mikerusoft.cassandra.pageslices.model.jpa.Slice;
import com.mikerusoft.cassandra.pageslices.urlreader.InputDataProcessor;
import com.mikerusoft.cassandra.pageslices.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.concurrent.CountDownLatch;

import static org.mockito.Mockito.*;

@SpringBootTest
@EmbeddedCassandra(scripts = {"/init.cql"})
@Slf4j
class FromUrlToCassandraFlowManagerTest {

    @Autowired
    private Cluster cluster;

    @Value("${spring.data.cassandra.keyspace-name:}")
    private String keyspace;

    @SpyBean
    private InputDataProcessor<String> processor;

    @Autowired
    private FromUrlToCassandraFlowManager flowManager;

    @Test
    void with2Fluxes_whenBothAreError_expectedNothingInserted() {
        doReturn(Flux.just(Utils.errorSlice(), Utils.errorSlice()))
                .when(processor).getSlices(any(String[].class));
        StepVerifier.create(flowManager.store())
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void with2Fluxes_whenFirstFluxIsError_expectedTheSecondOneInsertedIntoCassandra() {
        doReturn(Flux.just(Utils.errorSlice(), Slice.builder().content("Hello").slice(0).url("stam").build()))
                .when(processor).getSlices(any(String[].class));
        StepVerifier.create(flowManager.store())
                .expectNext(Slice.builder().content("Hello").slice(0).url("stam").build())
                .verifyComplete();
    }

    @Test
    @Disabled
    void realExample() throws Exception {
        cluster.connect(keyspace);

        CountDownLatch latch = new CountDownLatch(1);

        Disposable disposable = flowManager.store().doOnComplete(latch::countDown)
                .doOnError(t -> log.error("", t)).subscribe();

        latch.await();
        if (!disposable.isDisposed())
            disposable.dispose();
    }
}