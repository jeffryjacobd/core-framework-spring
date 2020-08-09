package com.jcb.web.handler.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcb.entity.SessionDataModel;
import com.jcb.entity.UserDetails;
import com.jcb.entity.UserModel;
import com.jcb.entity.WebSession;
import com.jcb.service.crypt.keygeneration.AESKeyGeneratorService;
import com.jcb.service.crypt.keygeneration.RSAKeyGeneratorService;
import com.jcb.service.security.AESEncryptionService;
import com.jcb.service.security.RSAEncryptionService;
import com.jcb.service.security.UserAuthenticationService;
import com.jcb.web.handler.LoginHandler;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

public class LoginHandlerImpl implements LoginHandler {
	@Autowired
	private RSAKeyGeneratorService rsaKeyGenerator;

	@Autowired
	private AESKeyGeneratorService aesKeyGenerator;

	@Autowired
	private UserAuthenticationService userAuthenticationService;

	@Autowired
	private AESEncryptionService aesEncryptionService;

	@Autowired
	private RSAEncryptionService rsaEncryptionService;

	@Override
	public Mono<ServerResponse> getSession(ServerRequest request) {
		return Mono.defer(() -> request.bodyToMono(SessionDataModel.class)).map(sessionModel -> {
			sessionModel.setRoute("home");
			return sessionModel;
		}).zipWhen(sessionModel -> {
			return this.rsaKeyGenerator.generatePublicKeyFromPem(sessionModel.getKey());
		}, (sessionModelCombinator, publicKey) -> {
			return Tuples.of(sessionModelCombinator, publicKey);
		}).flatMap(sessionModelPublicKeyTuple -> {
			return this.aesKeyGenerator.getAESKey().map(secretKey -> {
				return new String(Base64.getEncoder().encode(secretKey.getEncoded()), StandardCharsets.UTF_8);
			}).flatMap(secretKeyBase64 -> {
				return request.session().doOnNext(session -> {
					session.getAttributes().put(WebSession.ENCRYPTION_KEY, secretKeyBase64);
					session.getAttributes().put(WebSession.SAVE_ON_UPDATE, true);
				}).flatMap(session -> {
					String jsonModel = "";
					SessionDataModel model = sessionModelPublicKeyTuple.getT1();
					model.setKey(secretKeyBase64);
					try {
						jsonModel = new ObjectMapper().writeValueAsString(model);
					} catch (JsonProcessingException e) {
						e.printStackTrace();
						return Mono.error(e);
					}
					return Mono.just(jsonModel);
				});
			}).flatMap(jsonModel -> {
				return this.rsaEncryptionService.encrypt(jsonModel.getBytes(StandardCharsets.UTF_8),
						sessionModelPublicKeyTuple.getT2());
			}).map(encryptedResult -> {
				return Hex.encode(encryptedResult);
			}).flatMap(encryptedString -> {
				return ServerResponse.ok().bodyValue(new String(encryptedString, StandardCharsets.UTF_8));
			});
		});
	}

	@Override
	public Mono<ServerResponse> login(ServerRequest request) {
		return Mono.defer(() -> request.bodyToMono(UserModel.class).filter(userModel -> {
			return !((userModel.getUser().isBlank()) || userModel.getPassword().isBlank());
		}).flatMap(filteredUserModel -> {
			return this.userAuthenticationService.findByUsername(filteredUserModel.getUser()).filter(userDetail -> {
				return UserDetails.encoder.matches(filteredUserModel.getPassword(), userDetail.getPassword());
			});
		}).flatMap(userDetail -> {
			return saveLoginSession(request, userDetail);
		}).switchIfEmpty(Mono.defer(() -> ServerResponse.status(HttpStatus.UNAUTHORIZED).build())));

	}

	private Mono<ServerResponse> saveLoginSession(ServerRequest request,
			org.springframework.security.core.userdetails.UserDetails userDetail) {
		Mono<org.springframework.web.server.WebSession> webSession = request.session();
		return webSession.doOnNext(session -> {
			session.getAttributes().remove(WebSession.ENCRYPTION_KEY);
			session.getAttributes().put(WebSession.USER_NAME_KEY, userDetail.getUsername());
			session.getAttributes().put(WebSession.SAVE_ON_UPDATE, false);
		}).flatMap(session -> ServerResponse.ok().build());
	}

	@Override
	public Mono<ServerResponse> logout(ServerRequest request) {
		Mono<org.springframework.web.server.WebSession> webSession = request.session();
		return webSession.doOnNext(session -> {
			session.getAttributes().put(WebSession.USER_NAME_KEY, "");
			session.getAttributes().put(WebSession.SAVE_ON_UPDATE, true);
		}).flatMap(session -> ServerResponse.ok().build());
	}

	@Override
	public Mono<Void> doLogoutHandler(WebFilterExchange webFilterExchange, AuthenticationException exception) {
		return Mono.defer(() -> {
			return rsaKeyGenerator.getRSAKey()
					.zipWith(webFilterExchange.getExchange().getSession().defaultIfEmpty(WebSession.builder().build()))
					.filter(sessionPublicKeyTuple -> {
						return !sessionPublicKeyTuple.getT2().getId().isBlank();
					}).switchIfEmpty(Mono.defer(() -> {
						ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
						response.getHeaders().clearContentHeaders();
						response.setStatusCode(HttpStatus.UNAUTHORIZED);
						return Mono.empty();
					})).zipWhen(nonEmptyPublicKeySession -> {
						KeyPair rsaKey = nonEmptyPublicKeySession.getT1();
						org.springframework.web.server.WebSession session = nonEmptyPublicKeySession.getT2();
						session.getAttributes().put(WebSession.ENCRYPTION_KEY, rsaKey);
						RSAPublicKey publicKey = (RSAPublicKey) rsaKey.getPublic();
						StringWriter writer = new StringWriter();
						PemWriter pemWriter = new PemWriter(writer);
						try {
							pemWriter.writeObject(new PemObject("RSA PUBLIC KEY", publicKey.getEncoded()));
							pemWriter.flush();
						} catch (IOException e) {
							e.printStackTrace();
							return Mono.error(e);
						}
						return this.aesEncryptionService.encrypt(writer.toString().getBytes(StandardCharsets.UTF_8),
								session.getId().getBytes(StandardCharsets.UTF_8), true);
					}, (tuple, encryptedRsaKeyBytes) -> {
						return encryptedRsaKeyBytes;
					}).map(encryptedRsaKeyBytes -> new String(encryptedRsaKeyBytes, StandardCharsets.UTF_8))
					.flatMap(encrypedRsaKey -> {
						ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
						SessionDataModel model = new SessionDataModel();
						model.setRoute("login");
						model.setKey(encrypedRsaKey);
						byte[] responseBody = null;
						try {
							responseBody = new ObjectMapper().writeValueAsBytes(model);
						} catch (JsonProcessingException e) {
							return Mono.error(e);
						}
						DataBuffer data = response.bufferFactory().wrap(responseBody);
						response.getHeaders().add("Content-Type", "application/json");
						response.setStatusCode(HttpStatus.UNAUTHORIZED);
						return response.writeWith(Mono.just(data));
					});
		});
	}
}
