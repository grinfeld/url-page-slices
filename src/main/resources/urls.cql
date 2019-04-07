CREATE TABLE slices (
    url text,
    slice numeric,
    content BLOB,
    size NUMERIC,
    PRIMARY KEY (url, slice)
);