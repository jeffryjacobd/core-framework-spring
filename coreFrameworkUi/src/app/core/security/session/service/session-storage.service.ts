import { Injectable, Inject } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SessionStorageService {

  private _sessionStorageModel: SessionStorageModel = {};
  constructor() { }

  setSession(sessionId: string) {
    this._sessionStorageModel = { sessionId: sessionId };
  }

  getSession(): string {
    return this._sessionStorageModel.sessionId;
  }

  clearSession(): void {
    this._sessionStorageModel.sessionId = undefined;
    this._sessionStorageModel = undefined;
  }
}

interface SessionStorageModel {
  sessionId?: string;
}