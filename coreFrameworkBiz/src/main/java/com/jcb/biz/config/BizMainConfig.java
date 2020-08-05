package com.jcb.biz.config;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.jcb.service.crypt.keygeneration.AESKeyGeneratorService;
import com.jcb.service.crypt.keygeneration.RSAKeyGeneratorService;
import com.jcb.service.crypt.keygeneration.impl.AESKeyGeneratorServiceImpl;
import com.jcb.service.crypt.keygeneration.impl.RSAKeyGeneratorServiceImpl;
import com.jcb.service.security.config.AuthenticationConfig;
import com.jcb.web.handler.config.HandlerConfig;

@Import({ HandlerConfig.class, AuthenticationConfig.class })
@Configuration
public class BizMainConfig {

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	@Bean
	public AESKeyGeneratorService aesKeyGenerator() {
		return new AESKeyGeneratorServiceImpl();
	}

	@Bean
	public RSAKeyGeneratorService rsaKeyGenerator() throws NoSuchAlgorithmException, NoSuchProviderException {
		return new RSAKeyGeneratorServiceImpl();
	}
}
