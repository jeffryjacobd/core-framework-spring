package com.jcb.service.crypt.keygeneration.impl;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.jcb.service.crypt.keygeneration.RSAKeyGeneratorService;

import ch.qos.logback.classic.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class RSAKeyGeneratorServiceImpl implements RSAKeyGeneratorService {
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(RSAKeyGeneratorServiceImpl.class);
	private final KeyPairGenerator keyPairGenerator;
	private final KeyFactory kf;
	private static final Integer MAXIMUM_COUNT = 100;

	public static final Map<Integer, KeyPair> cacheMap = new ConcurrentHashMap<>(100);

	public RSAKeyGeneratorServiceImpl() throws NoSuchAlgorithmException, NoSuchProviderException {
		this.keyPairGenerator = KeyPairGenerator.getInstance("RSA", BouncyCastleProvider.PROVIDER_NAME);
		this.kf = KeyFactory.getInstance("RSA");
	}

	@Override
	public Mono<KeyPair> generateKeyPair() {
		keyPairGenerator.initialize(2048);
		return Mono.just(keyPairGenerator.generateKeyPair());
	}

	@PostConstruct
	public void initializeKeyPair() {
		Flux.range(0, MAXIMUM_COUNT).parallel(10).runOn(Schedulers.boundedElastic()).doOnNext(count -> {
			cacheMap.put(count, generateKeyPair().block());
		}).sequential().doOnComplete(() -> {
			LOG.info("Created {} RSA keys", MAXIMUM_COUNT);
		}).subscribe();
	}

	@Override
	public Flux<KeyPair> getRSAKey(Integer count) {
		return Flux.range(0, count).parallel(count).runOn(Schedulers.boundedElastic()).map((index) -> {
			return new Random().nextInt(MAXIMUM_COUNT);
		}).map(randomIndex -> {
			KeyPair keyToReturn = cacheMap.get(randomIndex);
			if (keyToReturn == null) {
				keyToReturn = generateKeyPair().block();
			}
			Mono.defer(() -> generateKeyPair()).subscribeOn(Schedulers.boundedElastic()).doOnNext(rsaKey -> {
				cacheMap.put(randomIndex, rsaKey);
			}).subscribe();
			return keyToReturn;
		}).sequential();
	}

	@Override
	public Mono<PublicKey> generatePublicKeyFromPem(String base64Pem) {
		X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(base64Pem));
		PublicKey pubKey = null;
		try {
			pubKey = kf.generatePublic(keySpecX509);
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
			return Mono.error(e);
		}
		return Mono.just(pubKey);
	}
}
