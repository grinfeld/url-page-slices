package com.mikerusoft.cassandra.pageslices.flow;

import com.mikerusoft.cassandra.pageslices.model.jpa.Slice;
import reactor.core.publisher.Flux;

public interface ApplicationFlowManager {
    Flux<Slice> store();
}
