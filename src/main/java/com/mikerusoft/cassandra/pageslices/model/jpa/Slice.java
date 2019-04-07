package com.mikerusoft.cassandra.pageslices.model.jpa;

import lombok.*;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderClassName = "Builder", toBuilder = true)
@Table("slices")
public class Slice {

    // since we search first by URL - means it should be partition key,
    // i.e. Cassandra will decide what node is responsible for partition where URL is placed
    @PrimaryKeyColumn(name = "url", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String url;

    // slice is secondary key, where records with the same URL but different slices should be placed on same node
    @PrimaryKeyColumn(name = "slice", ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
    private int slice;

    @Column
    private String content;
}
