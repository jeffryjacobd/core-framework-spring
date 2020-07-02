/**
 * 
 */
package com.jcb.dto;

import com.jcb.annotation.CassandraTable;
import com.jcb.annotation.ClusteringKeyColumn;
import com.jcb.annotation.PartitionKeyColumn;
import com.jcb.constants.enumeration.Gender;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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
@CassandraTable(keySpace = "core", tableName = "userAccount")
@Builder
public class UserAccountDto {

    @PartitionKeyColumn(0)
    private String userName;

    @ClusteringKeyColumn(0)
    private String firstName;

    @ClusteringKeyColumn(1)
    private String middleName;

    private String lastName;

    private String password;

    private UUID companyId;

    private LocalDate dateOfBirth;

    private Gender gender;

    private List<String> roles;

    private String email;

    private String phoneNo;

    private String extNo;

}
