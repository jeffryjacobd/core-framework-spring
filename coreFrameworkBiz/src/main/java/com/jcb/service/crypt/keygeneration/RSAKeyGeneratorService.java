package com.jcb.service.crypt.keygeneration;

import java.security.KeyPair;
import java.security.PublicKey;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RSAKeyGeneratorService {

	Mono<KeyPair> generateKeyPair();

	Mono<PublicKey> generatePublicKeyFromPem(String base64Pem);

	Flux<KeyPair> getRSAKey(Integer count);

	default Mono<KeyPair> getRSAKey() {
		return getRSAKey(1).next();
	}

}
