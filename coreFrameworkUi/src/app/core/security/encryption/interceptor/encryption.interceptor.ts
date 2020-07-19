import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpResponse,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, merge } from 'rxjs';
import * as forge from 'node-forge';
import { EncryptionService } from '../service/encryption.service'
import { mergeMap, map, tap } from 'rxjs/operators';
import { SessionDataModel } from '../../auth/model/session-data-model';
import { SessionStorageService } from '../../session/service/session-storage.service';
import { Router } from '@angular/router';

@Injectable()
export class EncryptionInterceptor implements HttpInterceptor {

  constructor(private encrptionService: EncryptionService, private sessionStorageService: SessionStorageService, private router: Router) { }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (request.url.endsWith('handshake')) {
      return this.handshakeFilter(request, next);
    } else if (request.url.endsWith('login')) {
      return this.loginFilter(request, next);
    } else {
      return next.handle(request);
    }
  }
  private loginFilter(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request);
  }

  private handshakeFilter(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return this.encrptionService.getRsaKeyPair().pipe(mergeMap(keyPair => {
      request = request.clone({ body: keyPair.publicKey });
      return next.handle(request).pipe(map(response => {
        if (response instanceof HttpResponse) {
          const deCrytedString = this.encrptionService.decryptRsaContent(response.body, keyPair.privateKey);
          const model: SessionDataModel = JSON.parse(deCrytedString);
          this.sessionStorageService.setEncryptionKey(model.key);
          this.sessionStorageService.setDecryptionKey(model.key);
          response.clone({ body: undefined });
        }
        return response;
      }), tap(undefined, errorResponse => {
        if ((errorResponse instanceof HttpErrorResponse) && (errorResponse.status != 200)) {
          this.router.navigateByUrl('login', { skipLocationChange: true, replaceUrl: false })
        }
      }));
    }));
  }
}
