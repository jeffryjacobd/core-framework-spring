import { Injectable, OnDestroy, OnInit } from '@angular/core';
import * as forge from 'node-forge';
import * as CryptoJS from 'crypto-js';
import { bindCallback, Observable, Subject, of, from } from 'rxjs';
import { map, tap, takeUntil, mergeMap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class EncryptionService implements OnInit, OnDestroy {

  private destroy$: Subject<boolean> = new Subject<boolean>();
  private _initialKeyPair: forge.pki.KeyPair;
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
      return this.createRsaKeyPair().pipe(tap(keyPair => {
        this._initialKeyPair = keyPair;
      }), takeUntil(this.destroy$)).subscribe();
    }, 20000);
  }

  stopRsaKeyPairCreation() {
    clearTimeout(this._rsaCreationInterval);
  }

  private createRsaKeyPair(): Observable<forge.pki.KeyPair> {
    const keyPairObservable = bindCallback(forge.pki.rsa.generateKeyPair);
    return keyPairObservable({ bits: 2048 }).pipe(map((result: [Error, forge.pki.KeyPair]) => {
      if (!!result[0]) {
        console.error(result[0]);
        throw result[0];
      }
      return result[1];
    }), takeUntil(this.destroy$));
  }

  getRsaKeyPair(): Observable<forge.pki.KeyPair> {
    if (this._initialKeyPair == undefined) {
      return this.createRsaKeyPair();
    } else {
      return of(this._initialKeyPair);
    }
  }

  decryptAesContentWithStringKey(body: any, secretKey: string, isHashedPassword: boolean = true): string {
    if (isHashedPassword) {
      const encrypted = CryptoJS.enc.Hex.parse(Buffer.from(body, 'base64').toString('hex'));
      const salt = CryptoJS.lib.WordArray.create(encrypted.words.slice(0, SALT_LENGTH / 4));
      const key = CryptoJS.PBKDF2(secretKey, salt, { keySize: 256 / 32, iterations: HASH_ITERATIONS, hasher: CryptoJS.algo.SHA256 });
      const decrypted = CryptoJS.AES.decrypt(CryptoJS.lib.WordArray.create(encrypted.words.slice(SALT_LENGTH / 4)).toString()
        , key, {
        format: CryptoJS.format.Hex,
        keySize: 32,
        mode: CryptoJS.mode.ECB,
        padding: CryptoJS.pad.Pkcs7
      });
      return decrypted.toString(CryptoJS.enc.Utf8);
    }
    return '';
  }

  encryptWithRsaPublicKeyString(body: any, publicKey: string): Observable<string> {
    const pemHeader = "-----BEGIN RSA PUBLIC KEY-----";
    const pemFooter = "-----END RSA PUBLIC KEY-----";
    const pemContents = publicKey.substring(pemHeader.length + 1, publicKey.length - pemFooter.length - 1);
    const binaryDerString = atob(pemContents);
    const binaryDer = this.str2ab(binaryDerString);
    return from(crypto.subtle.importKey('spki', binaryDer, { name: "RSA-OAEP", hash: "SHA-256" }, true, ["encrypt"])).pipe(mergeMap(encryptionKey => {
      return from(crypto.subtle.encrypt({ name: "RSA-OAEP" }, encryptionKey, new TextEncoder().encode(JSON.stringify(body))));
    }), map(arrayBuffer => {
      return Buffer.from(arrayBuffer).toString('hex');
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

  decryptRsaContent(body: any, privateKey: forge.pki.PrivateKey): string {
    const rsaPrivateKey: forge.pki.rsa.PrivateKey = forge.pki.privateKeyFromPem(forge.pki.privateKeyToPem(privateKey));
    return rsaPrivateKey.decrypt(body);
  }

}
const SALT_LENGTH = 16;
const HASH_ITERATIONS = 10000;