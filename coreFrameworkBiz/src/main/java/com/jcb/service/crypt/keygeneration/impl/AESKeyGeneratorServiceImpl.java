package com.jcb.service.crypt.keygeneration.impl;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.jcb.service.crypt.keygeneration.AESKeyGeneratorService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.gen.OctetSequenceKeyGenerator;

import ch.qos.logback.classic.Logger;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Component
public class AESKeyGeneratorServiceImpl implements AESKeyGeneratorService {
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(AESKeyGeneratorServiceImpl.class);
	private static final Integer MAXIMUM_COUNT = 100;

	public static final Map<Integer, OctetSequenceKey> cacheMap = new ConcurrentHashMap<>(100);

	@Override
	public OctetSequenceKey generateKeyPair() {
		OctetSequenceKey rsaKey = null;
		try {
			rsaKey = new OctetSequenceKeyGenerator(4096).keyUse(KeyUse.ENCRYPTION).keyID(UUID.randomUUID().toString())
					.generate();
		} catch (JOSEException e) {
			e.printStackTrace();
		}
		return rsaKey;
	}

	@PostConstruct
	public void initializeKeyPair() {
		Flux.range(0, MAXIMUM_COUNT).parallel(10).runOn(Schedulers.newBoundedElastic(10, 10, "AESCreation", 8, true))
				.doOnNext(count -> {
					cacheMap.put(count, generateKeyPair());
				}).sequential().doOnComplete(() -> {
					LOG.info("Created {} AES keys", MAXIMUM_COUNT);
				}).subscribe();
	}

	@Override
	public Flux<OctetSequenceKey> getAESKey(Integer count) {
		return Flux.range(0, count).parallel(count)
				.runOn(Schedulers.newBoundedElastic(count, count, "AESCreation", 5, false)).map((index) -> {
					return new Random().nextInt(MAXIMUM_COUNT);
				}).map(randomIndex -> {
					OctetSequenceKey keyToReturn = cacheMap.get(randomIndex);
					cacheMap.put(randomIndex, generateKeyPair());
					LOG.info("Created {} AES keys", count);
					return keyToReturn;
				}).sequential();
	}
}
