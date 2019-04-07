package com.mikerusoft.cassandra.pageslices.repositories;

import com.mikerusoft.cassandra.pageslices.model.jpa.Slice;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SliceReactiveRepository extends ReactiveCassandraRepository<Slice, String> {

    Flux<Slice> findByUrl(String url);

    Mono<Slice> findByUrlAndAndSlice(String url, int slice);

}
