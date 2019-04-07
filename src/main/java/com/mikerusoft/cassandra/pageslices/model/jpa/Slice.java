package com.mikerusoft.cassandra.pageslices.model.jpa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.Arrays;
import java.util.Objects;

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
    private byte[] content;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Slice slice1 = (Slice) o;
        return slice == slice1.slice &&
                Objects.equals(url, slice1.url) &&
                Arrays.equals(content, slice1.content);
    }
}
