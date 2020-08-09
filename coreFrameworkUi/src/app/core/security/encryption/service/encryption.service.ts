import { Injectable, OnDestroy, OnInit } from '@angular/core';
import { Observable, Subject, of, from, zip } from 'rxjs';
import { map, tap, takeUntil, mergeMap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class EncryptionService implements OnInit, OnDestroy {


  private destroy$: Subject<boolean> = new Subject<boolean>();
  private _initialKeyPair: CryptoKeyPair;
  private _rsaCreationInterval: NodeJS.Timeout;

  constructor() { }

  ngOnInit(): void {
    this.startRsaKeyPairCreation();
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
    this.destroy$.unsubscribe();
    this._rsaCreationInterval = undefined;
  }

  startRsaKeyPairCreation() {
    this._rsaCreationInterval = setInterval(() => {
      return from(this.createRsaKeyPair()).pipe(tap(keyPair => {
        this._initialKeyPair = keyPair;
      }), takeUntil(this.destroy$)).subscribe();
    }, 20000);
  }

  stopRsaKeyPairCreation() {
    clearTimeout(this._rsaCreationInterval);
  }

  private createRsaKeyPair(): PromiseLike<CryptoKeyPair> {
    return crypto.subtle.generateKey({
      name: 'RSA-OAEP', modulusLength: 2048,
      publicExponent: new Uint8Array([1, 0, 1]),
      hash: HASH
    }, true, [ENCRYPT, DECRYPT]);
  }

  getRsaKeyPair(): Observable<CryptoKeyPair> {
    if (this._initialKeyPair == undefined) {
      return from(this.createRsaKeyPair());
    } else {
      return of(this._initialKeyPair);
    }
  }

  convertRsaPublicKeyToBase64(publicKey: CryptoKey): Observable<string> {
    return from(crypto.subtle.exportKey("spki", publicKey)).pipe(map(exportedArrayBuffer => {
      return this.ab2str(exportedArrayBuffer);
    }), map(exportedString => {
      return btoa(exportedString);
    }));
  }

  decryptAesContentWithStringKey(body: any, secretKey: string): Observable<string> {
    return from(crypto.subtle.importKey("raw", new Int8Array(new TextEncoder().encode(secretKey)), PBKDF2, false, ["deriveKey"])).pipe(mergeMap(rawKey => {
      return from(crypto.subtle.deriveKey({
        name: PBKDF2, salt: new Int8Array(Buffer.from(body, BASE64).slice(0, SALT_LENGTH)),
        iterations: HASH_ITERATIONS, hash: HASH
      }, rawKey, { name: AES_GCM, length: KEY_LENGTH }, true, [DECRYPT]))
    }), mergeMap(derivedKey => {
      return from(crypto.subtle.decrypt({
        name: AES_GCM, iv: new Int8Array(Buffer.from(body, BASE64).slice(SALT_LENGTH, SALT_LENGTH + IV_LENGTH)),
        length: KEY_LENGTH,
        tagLength: KEY_LENGTH / 2
      }, derivedKey, new Int8Array(Buffer.from(body, BASE64).slice(SALT_LENGTH + IV_LENGTH))));
    }), map(decryptedArray => {
      return new TextDecoder().decode(decryptedArray);
    }))
  }


  encryptAesContentWithStringKey(body: string, secretKey: string): Observable<string> {
    const salt = crypto.getRandomValues(new Int8Array(SALT_LENGTH));
    const iv = crypto.getRandomValues(new Int8Array(IV_LENGTH));
    return from(crypto.subtle.importKey("raw", new Int8Array(new TextEncoder().encode(secretKey)), PBKDF2, false, ["deriveKey"])).pipe(mergeMap(rawKey => {
      return from(crypto.subtle.deriveKey({
        name: PBKDF2, salt: salt,
        iterations: HASH_ITERATIONS, hash: HASH
      }, rawKey, { name: AES_GCM, length: KEY_LENGTH }, true, [ENCRYPT]))
    }), mergeMap(derivedKey => {
      return from(crypto.subtle.encrypt({ name: AES_GCM, iv: iv, length: KEY_LENGTH, tagLength: KEY_LENGTH / 2 },
        derivedKey, new Int8Array(new TextEncoder().encode(body))));
    }), map(encryptedContent => {
      return Buffer.concat([Buffer.from(salt), Buffer.from(iv), Buffer.from(encryptedContent)]).toString(BASE64);
    }));
  }

  encryptWithRsaPublicKeyString(body: string, publicKey: string): Observable<string> {
    const pemContents = publicKey.substring(pemHeader.length + 1, publicKey.length - pemFooter.length - 1);
    const binaryDerString = atob(pemContents);
    const binaryDer = this.str2ab(binaryDerString);
    return from(crypto.subtle.importKey('spki', binaryDer, { name: "RSA-OAEP", hash: HASH }, true, [ENCRYPT])).pipe(mergeMap(encryptionKey => {
      return from(crypto.subtle.encrypt({ name: "RSA-OAEP" }, encryptionKey, new TextEncoder().encode(body)));
    }), map(arrayBuffer => {
      return Buffer.from(arrayBuffer).toString(HEX);
    }));
  }

  private str2ab(binaryString: string): ArrayBuffer {
    const buf = new ArrayBuffer(binaryString.length);
    const bufView = new Uint8Array(buf);
    for (let i = 0; i < binaryString.length; i++) {
      bufView[i] = binaryString.charCodeAt(i);
    }
    return buf;
  }

  private ab2str(arrayBuffer: ArrayBuffer) {
    return String.fromCharCode.apply(null, new Uint8Array(arrayBuffer));
  }

  decryptRsaContent(body: string, privateKey: CryptoKeyPair["privateKey"]): Observable<string> {
    return from(crypto.subtle.decrypt({ name: "RSA-OAEP" }, privateKey, new Int8Array(Buffer.from(body, 'hex')))).pipe(map(arrayBuffer => {
      return Buffer.from(arrayBuffer).toString('utf-8');
    }));
  }

}
const SALT_LENGTH = 16;
const IV_LENGTH = 16;
const HASH_ITERATIONS = 10000;
const HASH = 'SHA-256';
const KEY_LENGTH = 256;
const HEX = 'hex';
const BASE64 = 'base64';
const AES_GCM = 'AES-GCM';
const PBKDF2 = 'PBKDF2';
const ENCRYPT = 'encrypt';
const DECRYPT = 'decrypt';
const pemHeader = "-----BEGIN RSA PUBLIC KEY-----";
const pemFooter = "-----END RSA PUBLIC KEY-----";