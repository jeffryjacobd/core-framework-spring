package com.jcb.dto;

import com.jcb.annotation.CassandraTable;
import com.jcb.annotation.PartitionKeyColumn;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@CassandraTable(keySpace = "core", tableName = "role")
@Builder
public class RoleDto {

    @PartitionKeyColumn(0)
    private String role;

    private List<String> relatedRoles;

    private String description;

}
