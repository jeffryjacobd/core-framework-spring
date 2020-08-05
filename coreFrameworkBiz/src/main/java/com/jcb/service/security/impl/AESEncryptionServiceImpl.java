package com.jcb.service.security.impl;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.LoggerFactory;

import com.jcb.service.security.AESEncryptionService;

import ch.qos.logback.classic.Logger;
import reactor.core.publisher.Mono;

public class AESEncryptionServiceImpl implements AESEncryptionService {
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(AESEncryptionServiceImpl.class);
	private static final int ITERATION_COUNT = 10000;
	private static Cipher cipher;

	public AESEncryptionServiceImpl() throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
		this.secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		cipher = Cipher.getInstance("AES/ECB/PKCS7PADDING", BouncyCastleProvider.PROVIDER_NAME);
	}

	private SecretKeyFactory secretKeyFactory;

	@Override
	public Mono<byte[]> encrypt(final byte[] plainValue, final byte[] secretKey, boolean isHashPassword) {
		byte[] salt = new byte[16];
		List<Byte> byteList = new ArrayList<>();
		new SecureRandom().nextBytes(salt);
		SecretKey encrptKey;
		try {
			encrptKey = isHashPassword
					? new SecretKeySpec(hashPassword(new String(secretKey, StandardCharsets.UTF_8).toCharArray(), salt),
							"AES")
					: new SecretKeySpec(secretKey, "AES");
			synchronized (cipher) {
				cipher.init(Cipher.ENCRYPT_MODE, encrptKey);
				byteList.addAll(Arrays.asList(ArrayUtils.toObject(salt)));
				byteList.addAll(Arrays.asList(ArrayUtils.toObject(cipher.doFinal(plainValue))));
			}
		} catch (InvalidKeySpecException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			return Mono.error(e);
		}
		return Mono.just(Base64.getEncoder().encode(ArrayUtils.toPrimitive(byteList.toArray(new Byte[0]))));
	}

	@Override
	public byte[] decrypt(final byte[] plainValue, final byte[] secretKey, final Map<String, Object> additionalParams) {
		return null;
	}

	private byte[] hashPassword(final char[] password, final byte[] salt) throws InvalidKeySpecException {
		return secretKeyFactory.generateSecret(new PBEKeySpec(password, salt, ITERATION_COUNT, 256)).getEncoded();
	}

}
