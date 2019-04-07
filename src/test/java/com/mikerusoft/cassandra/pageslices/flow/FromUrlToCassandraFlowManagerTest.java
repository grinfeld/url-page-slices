package com.mikerusoft.cassandra.pageslices.flow;

import com.datastax.driver.core.Cluster;
import com.github.nosan.embedded.cassandra.spring.EmbeddedCassandra;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

@EmbeddedCassandra(scripts = "/urls.cql")
class FromUrlToCassandraFlowManagerTest {

    @Autowired /* only if @EmbeddedCassandra(replace = ANY) */
    private Cluster cluster;

    @Test
    public void test() {
        System.out.println();
    }
}