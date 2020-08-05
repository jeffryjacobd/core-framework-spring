package com.jcb.service.crypt.keygeneration;

import java.security.KeyPair;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RSAKeyGeneratorService {

	Mono<KeyPair> generateKeyPair();

	Flux<KeyPair> getRSAKey(Integer count);

	default Mono<KeyPair> getRSAKey() {
		return getRSAKey(1).next();
	}

}
