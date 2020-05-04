package com.jcb.dto;

import com.jcb.annotation.ClusteringKeyColumn;
import com.jcb.annotation.PartitionKeyColumn;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LogbackConfigDto {

    @PartitionKeyColumn(0)
    private String name;

    @ClusteringKeyColumn(0)
    private Integer rowElementNo;

    @ClusteringKeyColumn(1)
    private Integer containsInRowElement;

    private Map<String, List<String>> elementXMLValue;

}
