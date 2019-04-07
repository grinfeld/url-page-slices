package com.mikerusoft.cassandra.pageslices.urlreader;

import com.mikerusoft.cassandra.pageslices.model.jpa.Slice;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Component
public class InputFromUrlsDataProcessor implements InputDataProcessor<String> {

    private ReadingSlicesService<String> readingSlices;

    public InputFromUrlsDataProcessor(ReadingSlicesService<String> readingSlices) {
        this.readingSlices = readingSlices;
    }

    @Override
    public Flux<Slice> getSlices(String[] sources) {
        if (sources == null)
            return Flux.error(new IllegalArgumentException("sources couldn't be null"));
        return Flux.fromArray(sources).flatMap(s ->
            readingSlices.readFrom(s).doOnNext(d -> System.out.println(Thread.currentThread().getName() + " --- " + d))
                .subscribeOn(Schedulers.parallel())
        ).doOnNext(d -> System.out.println(Thread.currentThread().getName() + " --- " + d));
    }
}
