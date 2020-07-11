/**
 * 
 */
package com.jcb.dto;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.jcb.annotation.CassandraTable;
import com.jcb.annotation.ClusteringKeyColumn;
import com.jcb.annotation.PartitionKeyColumn;
import com.jcb.annotation.TWCSCompaction;
import com.jcb.annotation.TWCSCompaction.TimeWindowUnit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Jeffry Jacob D
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@CassandraTable(keySpace = "core", tableName = "session", ttl = 1801)
@TWCSCompaction(tombstoneThreshold = 0.09, compactionWindowUnit = TimeWindowUnit.DAYS, compactionWindowSize = 10, tombStoneCompactionInterval = 1801)
@Builder
public class SessionDto {

	@PartitionKeyColumn(0)
	private UUID sessionId;

	@ClusteringKeyColumn(0)
	private String ipAddress;

	@ClusteringKeyColumn(1)
	private String userName;

	private Map<String, Object> attributes;

	private boolean started;

	private boolean expired;

	private Instant creationTime;

	private Instant lastAccessTime;

	private Duration maxIdleTime;

}
