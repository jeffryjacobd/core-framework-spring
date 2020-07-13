import { Injectable, Inject } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SessionStorageService {

  private _sessionStorageModel: SessionStorageModel = {};
  constructor() { }

  setEncryptionKey(encryptionKey: string) {
    this._sessionStorageModel.encryptionKey = encryptionKey;
  }

  getEncryptionKey() {
    return this._sessionStorageModel.encryptionKey;
  }

  setLoginTime() {
    this._sessionStorageModel.loginTime = Date.now();
  }

  getLoginTime(): number {
    return this._sessionStorageModel.loginTime;
  }

  setSession(sessionId: string) {
    this._sessionStorageModel.sessionId = sessionId;
  }

  getSession(): string {
    return this._sessionStorageModel.sessionId;
  }

  clearSession(): void {
    this._sessionStorageModel.sessionId = undefined;
    this._sessionStorageModel.loginTime = undefined;
    this._sessionStorageModel.encryptionKey = undefined;
    this._sessionStorageModel = undefined;
  }
}

interface SessionStorageModel {
  sessionId?: string;
  loginTime?: number;
  encryptionKey?: string;
}