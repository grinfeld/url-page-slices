package com.mikerusoft.cassandra.pageslices.urlreader;

import com.mikerusoft.cassandra.pageslices.model.jpa.Slice;
import reactor.core.publisher.Flux;

public interface ReadingSlicesService<S> {

    // maybe better to add some SlicePojo without jpa configuration, so it could be "transitive" via REST or any other API,
    // without exposing vulnerabilities
    Flux<Slice> readFrom (S source);
}
