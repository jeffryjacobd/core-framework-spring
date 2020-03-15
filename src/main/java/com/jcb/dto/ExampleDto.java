/**
 * 
 */
package com.jcb.dto;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.jcb.annotation.CassandraTable;
import com.jcb.annotation.RedisTable;
import com.jcb.enumeration.Gender;

import java.time.LocalDate;

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
@CassandraTable(keySpace = "core", tableName = "Example_Dto")
@RedisTable
@Builder
public class ExampleDto {

    @PartitionKey(0)
    private Integer id;

    @ClusteringColumn(0)
    private String firstName;

    @ClusteringColumn(1)
    private String middleName;

    private String lastName;

    private String password;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate dateOfBirth;

    private Gender gender;

}
