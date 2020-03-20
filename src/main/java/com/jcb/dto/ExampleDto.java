/**
 * 
 */
package com.jcb.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.jcb.annotation.CassandraTable;
import com.jcb.annotation.ClusteringKeyColumn;
import com.jcb.annotation.PartitionKeyColumn;
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
@CassandraTable(keySpace = "core", tableName = "Example")
@RedisTable
@Builder
public class ExampleDto {

    @PartitionKeyColumn(0)
    private Integer id;

    @ClusteringKeyColumn(0)
    private String firstName;

    @ClusteringKeyColumn(1)
    private String middleName;

    private String lastName;

    private String password;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate dateOfBirth;

    private Gender gender;

}
