package com.mikerusoft.cassandra.pageslices.repositories;

import com.mikerusoft.cassandra.pageslices.model.jpa.Slice;
import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SliceReactiveRepository extends ReactiveCassandraRepository<Slice, String> {

    @AllowFiltering
    Flux<Slice> findByUrl(String url);

    @AllowFiltering
    Mono<Slice> findByUrlAndAndSlice(String url, int slice);

}
