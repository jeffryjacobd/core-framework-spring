package com.jcb.service.security;

import java.security.PrivateKey;
import java.security.PublicKey;

import reactor.core.publisher.Mono;

public interface RSAEncryptionService {

	Mono<byte[]> decrypt(byte[] cipherText, PrivateKey secretKey);

	Mono<byte[]> encrypt(byte[] plainText, PublicKey secretKey);

}
