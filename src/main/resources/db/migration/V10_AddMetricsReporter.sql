CREATE TABLE MetricsReporterInfo
(
    REPORTER_ID INT,
    OPPRETTET   TIMESTAMP,
    PRIMARY KEY (REPORTER_ID)
);

INSERT INTO MetricsReporterInfo(REPORTER_ID, OPPRETTET) VALUE (1, '2000-01-01 00:00:01')