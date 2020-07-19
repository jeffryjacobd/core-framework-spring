import { Injectable, OnDestroy, OnInit } from '@angular/core';
import * as forge from 'node-forge';
import { bindCallback, Observable, Subject, of } from 'rxjs';
import { map, tap, takeUntil } from 'rxjs/operators';
import { SessionStorageService } from '../../session/service/session-storage.service';

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

  decryptRsaContent(body: any, privateKey: forge.pki.PrivateKey): string {
    const rsaPrivateKey: forge.pki.rsa.PrivateKey = forge.pki.privateKeyFromPem(forge.pki.privateKeyToPem(privateKey));
    return rsaPrivateKey.decrypt(body);
  }
}
