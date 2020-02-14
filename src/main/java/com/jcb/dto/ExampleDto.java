/**
 * 
 */
package com.jcb.dto;

import com.jcb.annotation.RedisTable;

import java.time.LocalDate;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

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
@RedisTable
@Builder
public class ExampleDto {

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 0)
    private String id;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 1)
    private String name;

    private String password;

    private LocalDate dateOfBirth;

    private Gender gender;

}

enum Gender {
    MALE, FEMALE, NONE
}
