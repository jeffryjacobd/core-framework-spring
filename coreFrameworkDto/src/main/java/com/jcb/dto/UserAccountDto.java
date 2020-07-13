/**
 * 
 */
package com.jcb.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.jcb.annotation.CassandraTable;
import com.jcb.annotation.PartitionKeyColumn;
import com.jcb.constants.enumeration.Gender;

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

	private String lastName;

	private String password;

	private UUID companyId;

	private LocalDate dateOfBirth;

	private Gender gender;

	private List<String> roles;

	private String email;

	private String phoneNo;

	private String extNo;

	private boolean accountExpired;

	private boolean accountLocked;

	private boolean credentialsExpired;

	private boolean enabled;

}
