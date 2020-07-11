package com.jcb.service.crypt.keygeneration;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.OctetSequenceKey;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AESKeyGeneratorService {

	OctetSequenceKey generateKeyPair() throws JOSEException;

	Flux<OctetSequenceKey> getAESKey(Integer count);

	default Mono<OctetSequenceKey> getAESKey() {
		return getAESKey(1).next();
	}

}
