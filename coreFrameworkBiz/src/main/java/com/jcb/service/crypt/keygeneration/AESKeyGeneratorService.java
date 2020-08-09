package com.jcb.service.crypt.keygeneration;

import javax.crypto.SecretKey;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AESKeyGeneratorService {

	SecretKey generateKeyPair();

	Flux<SecretKey> getAESKey(Integer count);

	default Mono<SecretKey> getAESKey() {
		return getAESKey(1).next();
	}

}
