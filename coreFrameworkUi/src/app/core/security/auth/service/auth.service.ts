import { Injectable, Inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { UserModel } from '../model/user-model';
import { SessionDataModel } from '../model/session-data-model';
import { SessionStorageService } from '../../session/service/session-storage.service';
import { LOCAL_STORAGE, StorageService } from 'ngx-webstorage-service';
import { Observable, of, CompletionObserver } from 'rxjs';
import { map, tap, mergeMap } from 'rxjs/operators';
import { Router } from '@angular/router';

const SESSION_KEY = 'sessionId';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private loginCompleteObserver: CompletionObserver<any> = <CompletionObserver<any>>{
    complete: () => {
      this.sessionStorageService.setLoginTime();
    }
  };

  constructor(private http: HttpClient, private sessionStorageService: SessionStorageService, @Inject(LOCAL_STORAGE) private storage: StorageService, private router: Router) { }

  isAuthenticated(): Observable<string> {
    if (this.sessionStorageService.getLoginTime() != undefined) {
      return of('true');
    }
    return this._getSession();
  }

  login(userModel: UserModel) {
    return this.http.post<any>('login', userModel).pipe(tap(this.loginCompleteObserver), mergeMap((loginResponse) => {
      return this.http.post<SessionDataModel>('getSession', {}).pipe(tap(
        sessionData => {
          !!sessionData.key && this.sessionStorageService.setEncryptionKey(sessionData.key);
          this.sessionStorageService.setLoginTime();
          this.router.navigateByUrl(sessionData.route, { skipLocationChange: true, replaceUrl: false });
        }
      ));
    }));
  }

  private _getSession(): Observable<string> {
    const localStorageSessionKey = this.storage.get(SESSION_KEY);
    if (localStorageSessionKey != undefined) {
      this.sessionStorageService.setSession(localStorageSessionKey);
    }
    return this.http.post<SessionDataModel>('getSession', {}).pipe(tap(
      sessionData => {
        !!sessionData.key && this.sessionStorageService.setEncryptionKey(sessionData.key);
        this.sessionStorageService.setLoginTime();
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
