package com.jcb.service.crypt.keygeneration.impl;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.jcb.service.crypt.keygeneration.RSAKeyGeneratorService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;

import ch.qos.logback.classic.Logger;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Component
public class RSAKeyGeneratorServiceImpl implements RSAKeyGeneratorService {
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(RSAKeyGeneratorServiceImpl.class);
	private static final Integer MAXIMUM_COUNT = 100;

	public static final Map<Integer, RSAKey> cacheMap = new ConcurrentHashMap<>(100);

	@Override
	public RSAKey generateKeyPair() {
		RSAKey rsaKey = null;
		try {
			rsaKey = new RSAKeyGenerator(4096).keyUse(KeyUse.ENCRYPTION).keyID(UUID.randomUUID().toString()).generate();
		} catch (JOSEException e) {
			e.printStackTrace();
		}
		return rsaKey;
	}

	@PostConstruct
	public void initializeKeyPair() {
		Flux.range(0, MAXIMUM_COUNT).parallel(10).runOn(Schedulers.newBoundedElastic(10, 10, "RSACreation", 8, true))
				.doOnNext(count -> {
					cacheMap.put(count, generateKeyPair());
				}).sequential().doOnComplete(() -> {
					LOG.info("Created {} RSA keys", MAXIMUM_COUNT);
				}).subscribe();
	}

	@Override
	public Flux<RSAKey> getRSAKey(Integer count) {
		return Flux.range(0, count).parallel(count).runOn(Schedulers.boundedElastic()).map((index) -> {
			return new Random().nextInt(MAXIMUM_COUNT);
		}).map(randomIndex -> {
			RSAKey keyToReturn = cacheMap.get(randomIndex);
			cacheMap.put(randomIndex, generateKeyPair());
			if (keyToReturn == null) {
				keyToReturn = cacheMap.get(randomIndex);
			}
			LOG.info("Created {} RSA key pairs", count);
			return keyToReturn;
		}).sequential();
	}
}
