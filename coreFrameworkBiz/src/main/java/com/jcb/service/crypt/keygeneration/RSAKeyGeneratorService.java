package com.jcb.service.crypt.keygeneration;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RSAKeyGeneratorService {

	RSAKey generateKeyPair() throws JOSEException;

	Flux<RSAKey> getRSAKey(Integer count);

	default Mono<RSAKey> getRSAKey() {
		return getRSAKey(1).next();
	}

}
