package com.jcb.service.security;

import reactor.core.publisher.Mono;

public interface AESEncryptionService {

	Mono<byte[]> encrypt(byte[] plainValue, byte[] secretKey, boolean isHashPassword);

	Mono<byte[]> decrypt(byte[] cipherValue, byte[] secretKey, boolean isHashPassword);

}
