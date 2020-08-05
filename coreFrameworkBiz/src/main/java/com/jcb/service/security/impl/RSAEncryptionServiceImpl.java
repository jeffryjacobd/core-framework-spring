package com.jcb.service.security.impl;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.MGF1ParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.jcb.service.security.RSAEncryptionService;

import reactor.core.publisher.Mono;

public class RSAEncryptionServiceImpl implements RSAEncryptionService {

	private Cipher cipher;

	private OAEPParameterSpec oaepParameterSpec;

	public RSAEncryptionServiceImpl() throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {
		cipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding", BouncyCastleProvider.PROVIDER_NAME);
		oaepParameterSpec = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256,
				PSource.PSpecified.DEFAULT);
	}

	@Override
	public synchronized Mono<byte[]> decrypt(byte[] cipherText, KeyPair secretKey) {
		try {
			cipher.init(Cipher.DECRYPT_MODE, secretKey.getPrivate(), oaepParameterSpec);
			return Mono.just(cipher.doFinal(cipherText));
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException
				| InvalidAlgorithmParameterException e) {
			e.printStackTrace();
			return Mono.error(e);
		}
	}

}
