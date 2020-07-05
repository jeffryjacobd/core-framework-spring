import { Injectable, Inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { UserModel } from '../model/user-model';
import { SessionDataModel } from '../model/session-data-model';
import { SessionStorageService } from '../../session/service/session-storage.service';
import { LOCAL_STORAGE, StorageService } from 'ngx-webstorage-service';
import { Observable, of } from 'rxjs';
import { map, tap } from 'rxjs/operators';

const SESSION_KEY = 'sessionId';

@Injectable({
  providedIn: 'root'
})
export class AuthService {


  constructor(private http: HttpClient, private sessionStorageService: SessionStorageService, @Inject(LOCAL_STORAGE) private storage: StorageService) { }

  isAuthenticated(): Observable<string> {
    if (this.sessionStorageService.getSession() != undefined) {
      return of('true');
    }
    return this._getSession();
  }

  login(userModel: UserModel) {
    if (userModel.rememberMe) {
      //TODO local session store
    }
  }

  private _getSession(): Observable<string> {
    let sessionDataModel: SessionDataModel = {};
    const localStorageSessionKey = this.storage.get(SESSION_KEY);
    if (localStorageSessionKey != undefined) {
      sessionDataModel = { sessionId: localStorageSessionKey };
    }
    return this.http.post<SessionDataModel>('getSession', sessionDataModel).pipe(tap(
      sessionData => {
        if ((sessionData.route != undefined) && (sessionData.route != 'login')) {
          this.sessionStorageService.setSession(sessionData.sessionId);
          !!sessionDataModel.sessionId && this.storage.set(SESSION_KEY, sessionData.sessionId);
        }
      }
    ),
      map(
        sessionData => {
          return sessionData.route;
        }
      ));
  }

  logout() {
    this.sessionStorageService.clearSession();
    this.storage.remove(SESSION_KEY);
  }
}
