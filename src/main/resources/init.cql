CREATE KEYSPACE IF NOT EXISTS example
  WITH REPLICATION = {
   'class' : 'SimpleStrategy',
   'replication_factor' : 1
  };

USE example;

CREATE TABLE IF NOT EXISTS slices (
     url varchar,
     slice int,
     content text,
     PRIMARY KEY ((url), slice)
) WITH CLUSTERING ORDER BY (slice ASC);