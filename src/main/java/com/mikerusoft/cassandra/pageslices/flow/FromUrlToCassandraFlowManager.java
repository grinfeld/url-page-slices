package com.mikerusoft.cassandra.pageslices.flow;

import com.mikerusoft.cassandra.pageslices.conf.UrlConfig;
import com.mikerusoft.cassandra.pageslices.model.jpa.Slice;
import com.mikerusoft.cassandra.pageslices.repositories.SliceReactiveRepository;
import com.mikerusoft.cassandra.pageslices.urlreader.InputDataProcessor;
import com.mikerusoft.cassandra.pageslices.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Component
@Slf4j
public class FromUrlToCassandraFlowManager implements ApplicationFlowManager {

    private InputDataProcessor<String> inputDataProcessor;
    private SliceReactiveRepository repository;
    private String[] urls;

    @Autowired
    public FromUrlToCassandraFlowManager(InputDataProcessor<String> inputDataProcessor,
                                                 SliceReactiveRepository repository,
                                                 UrlConfig urlConfig) {
        this.inputDataProcessor = inputDataProcessor;
        this.repository = repository;
        this.urls = urlConfig.getUrls();
    }

    @Override
    public Flux<Slice> store() {
        return inputDataProcessor.getSlices(urls)
                .onErrorReturn(Utils.errorSlice())
        // let's filter error, for example if one of url was unreachable, we want other urls to be processed
                .filter(s -> !Utils.isError(s))
            // we want to insert into cassandra using parallel, too - so, let's parallelize this in line below
            .flatMap(s -> repository.insert(s).retry(2).subscribeOn(Schedulers.parallel()));
                                        // let's add another 2 retries for insert operation
    }
}
