package com.mikerusoft.cassandra.pageslices.flow;

import com.mikerusoft.cassandra.pageslices.model.jpa.Slice;
import com.mikerusoft.cassandra.pageslices.repositories.SliceReactiveRepository;
import com.mikerusoft.cassandra.pageslices.urlreader.InputDataProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Component
public class FromUrlToCassandraFlowManager implements ApplicationFlowManager {

    private InputDataProcessor<String> inputDataProcessor;
    private SliceReactiveRepository repository;
    private String[] urls;

    @Autowired
    public FromUrlToCassandraFlowManager(InputDataProcessor<String> inputDataProcessor,
                                                 SliceReactiveRepository repository,
                                                 @Value("${slices.urls}") String[] urls) {
        this.inputDataProcessor = inputDataProcessor;
        this.repository = repository;
        this.urls = urls;
    }

    @Override
    public Flux<Slice> store() {
        return inputDataProcessor.getSlices(urls)
                .flatMap(s -> repository.insert(s).subscribeOn(Schedulers.parallel()));
    }
}
