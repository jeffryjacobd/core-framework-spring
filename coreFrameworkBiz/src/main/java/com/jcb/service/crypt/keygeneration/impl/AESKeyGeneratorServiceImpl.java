package com.jcb.service.crypt.keygeneration.impl;

import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.jcb.service.crypt.keygeneration.AESKeyGeneratorService;

import ch.qos.logback.classic.Logger;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Component
public class AESKeyGeneratorServiceImpl implements AESKeyGeneratorService {
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(AESKeyGeneratorServiceImpl.class);
	private static final Integer MAXIMUM_COUNT = 100;

	public static final Map<Integer, SecretKey> cacheMap = new ConcurrentHashMap<>(100);
	private final KeyGenerator keyGen;

	public AESKeyGeneratorServiceImpl() throws NoSuchAlgorithmException {
		keyGen = KeyGenerator.getInstance("AES");
	}

	@Override
	public SecretKey generateKeyPair() {
		keyGen.init(256);
		return keyGen.generateKey();
	}

	@PostConstruct
	public void initializeKeyPair() {
		Flux.range(0, MAXIMUM_COUNT).parallel(10).runOn(Schedulers.boundedElastic()).doOnNext(count -> {
			cacheMap.put(count, generateKeyPair());
		}).sequential().doOnComplete(() -> {
			LOG.info("Created {} AES keys", MAXIMUM_COUNT);
		}).subscribe();
	}

	@Override
	public Flux<SecretKey> getAESKey(Integer count) {
		return Flux.range(0, count).parallel(count).runOn(Schedulers.boundedElastic()).map((index) -> {
			return new Random().nextInt(MAXIMUM_COUNT);
		}).map(randomIndex -> {
			SecretKey keyToReturn = cacheMap.get(randomIndex);
			cacheMap.put(randomIndex, generateKeyPair());
			if (keyToReturn == null) {
				keyToReturn = cacheMap.get(randomIndex);
			}
			LOG.info("Created {} AES keys", count);
			return keyToReturn;
		}).sequential();
	}
}
