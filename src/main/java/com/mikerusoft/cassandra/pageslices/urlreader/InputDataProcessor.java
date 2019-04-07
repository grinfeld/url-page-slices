package com.mikerusoft.cassandra.pageslices.urlreader;

import com.mikerusoft.cassandra.pageslices.model.jpa.Slice;
import reactor.core.publisher.Flux;

public interface InputDataProcessor<S> {
    Flux<Slice> getSlices(S[] sources);
}
