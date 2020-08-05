package com.jcb.service.security;

import java.security.KeyPair;

import reactor.core.publisher.Mono;

public interface RSAEncryptionService {

	Mono<byte[]> decrypt(byte[] cipherText, KeyPair secretKey);

}
