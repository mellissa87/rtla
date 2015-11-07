CREATE KEYSPACE rtla WITH replication = {
  'class': 'NetworkTopologyStrategy',
  'datacenter1': '1'
};

USE rtla;

CREATE TABLE errorlogs (
  host text,
  time timestamp,
  thread text,
  logger text,
  message text,
  PRIMARY KEY ((host), time, logger)
) WITH COMPACT STORAGE
  AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
  AND compaction = {'class': 'DateTieredCompactionStrategy', 'timestamp_resolution': 'MILLISECONDS', 'base_time_seconds': '3600', 'min_threshold': '4', 'max_sstable_age_days': '30'}
  AND gc_grace_seconds = 0
  AND read_repair_chance = 0.0
  AND dclocal_read_repair_chance = 0.0
  AND CLUSTERING ORDER BY (time DESC);

CREATE TABLE warnlogs (
  host text,
  time timestamp,
  thread text,
  logger text,
  message text,
  PRIMARY KEY ((host), time, logger)
) WITH COMPACT STORAGE
  AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
  AND compaction = {'class': 'DateTieredCompactionStrategy', 'timestamp_resolution': 'MILLISECONDS', 'base_time_seconds': '3600', 'min_threshold': '4', 'max_sstable_age_days': '30'}
  AND gc_grace_seconds = 0
  AND read_repair_chance = 0.0
  AND dclocal_read_repair_chance = 0.0
  AND CLUSTERING ORDER BY (time DESC);

CREATE TABLE infologs (
  host text,
  time timestamp,
  thread text,
  logger text,
  message text,
  PRIMARY KEY ((host), time, logger)
) WITH COMPACT STORAGE
  AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
  AND compaction = {'class': 'DateTieredCompactionStrategy', 'timestamp_resolution': 'MILLISECONDS', 'base_time_seconds': '3600', 'min_threshold': '4', 'max_sstable_age_days': '30'}
  AND gc_grace_seconds = 0
  AND read_repair_chance = 0.0
  AND dclocal_read_repair_chance = 0.0
  AND CLUSTERING ORDER BY (time DESC);

CREATE TABLE debuglogs (
  host text,
  time timestamp,
  thread text,
  logger text,
  message text,
  PRIMARY KEY ((host), time, logger)
) WITH COMPACT STORAGE
  AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
  AND compaction = {'class': 'DateTieredCompactionStrategy', 'timestamp_resolution': 'MILLISECONDS', 'base_time_seconds': '3600', 'min_threshold': '4', 'max_sstable_age_days': '30'}
  AND gc_grace_seconds = 0
  AND read_repair_chance = 0.0
  AND dclocal_read_repair_chance = 0.0
  AND CLUSTERING ORDER BY (time DESC);

CREATE TABLE tracelogs (
  host text,
  time timestamp,
  thread text,
  logger text,
  message text,
  PRIMARY KEY ((host), time, logger)
) WITH COMPACT STORAGE
  AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
  AND compaction = {'class': 'DateTieredCompactionStrategy', 'timestamp_resolution': 'MILLISECONDS', 'base_time_seconds': '3600', 'min_threshold': '4', 'max_sstable_age_days': '30'}
  AND gc_grace_seconds = 0
  AND read_repair_chance = 0.0
  AND dclocal_read_repair_chance = 0.0
  AND CLUSTERING ORDER BY (time DESC);
