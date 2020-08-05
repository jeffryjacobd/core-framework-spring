package com.jcb.service.security;

import java.util.Map;

import reactor.core.publisher.Mono;

public interface AESEncryptionService {

	Mono<byte[]> encrypt(byte[] plainValue, byte[] secretKey, boolean isHashPassword);

	byte[] decrypt(byte[] plainValue, byte[] secretKey, Map<String, Object> additionalParams);

}
