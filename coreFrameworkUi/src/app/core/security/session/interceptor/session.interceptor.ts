import { Injectable, Inject } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpResponse,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, from } from 'rxjs';
import { SessionStorageService } from '../service/session-storage.service';
import { tap } from 'rxjs/operators';
import { StorageService, LOCAL_STORAGE } from 'ngx-webstorage-service';
import { Router } from '@angular/router';
import { SessionDataModel } from '../../auth/model/session-data-model';
import { EncryptionService } from '../../encryption/service/encryption.service';

@Injectable({
  providedIn: 'root'
})
export class SessionInterceptor implements HttpInterceptor {
  constructor(private sessionStorageService: SessionStorageService, @Inject(LOCAL_STORAGE) private storage: StorageService, private router: Router, private encryptionService: EncryptionService) { }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    (this.sessionStorageService.getSession() != undefined) && (request = request.clone({ setHeaders: { [SESSION_TOKEN_KEY]: this.sessionStorageService.getSession() } }));
    return next.handle(request).pipe(tap(
      (event) => {
        if (event instanceof HttpResponse) {
          const authToken = event.headers.get(SESSION_TOKEN_KEY);
          this.sessionStorageService.setSession(authToken);
          this.storage.set(SESSION_KEY, authToken);
        }
      }, (event) => {
        if (event instanceof HttpErrorResponse) {
          const authToken = event.headers.get(SESSION_TOKEN_KEY);
          this.sessionStorageService.setSession(authToken);
          this.storage.set(SESSION_KEY, authToken);
          if (event.status.valueOf() == 401) {
            const model: SessionDataModel = JSON.parse(event.error);
            this.encryptionService.decryptAesContentWithStringKey(model.key, this.sessionStorageService.getSession()).pipe(tap(decryptedRsaKey => {
              this.sessionStorageService.setEncryptionKey(decryptedRsaKey);
            })).subscribe();
            this.router.navigateByUrl(model.route, { skipLocationChange: true, replaceUrl: false });
          }
        }
      }
    ));
  }
}
const SESSION_TOKEN_KEY = 'X-Auth-Token';
const SESSION_KEY = 'sessionId';