package com.jcb.dao.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.jcb.dao.UserAccountDao;
import com.jcb.dto.UserAccountDto;

import reactor.core.publisher.Mono;

@Component
public class UserAccountDaoImpl extends AbstractDaoImpl<UserAccountDto> implements UserAccountDao {

	@Override
	public Mono<UserAccountDto> get(String userName) {
		Map<String, Object> partitionKeyMap = new HashMap<>();
		partitionKeyMap.put("userName", userName);
		return super.getForPartitionKey(partitionKeyMap).next();
	}
}