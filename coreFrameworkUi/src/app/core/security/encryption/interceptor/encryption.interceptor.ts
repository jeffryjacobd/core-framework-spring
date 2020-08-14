import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpResponse,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, merge, of } from 'rxjs';
import { EncryptionService } from '../service/encryption.service'
import { mergeMap, map, tap } from 'rxjs/operators';
import { SessionDataModel } from '../../auth/model/session-data-model';
import { SessionStorageService } from '../../session/service/session-storage.service';

@Injectable()
export class EncryptionInterceptor implements HttpInterceptor {

  constructor(private encrptionService: EncryptionService, private sessionStorageService: SessionStorageService) { }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (request.url.endsWith('getSession')) {
      return this.handshakeFilter(request, next);
    } else if (request.url.endsWith('login')) {
      return this.loginFilter(request, next);
    } else {
      return next.handle(request);
    }
  }
  private loginFilter(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return this.encrptionService.encryptWithRsaPublicKeyString(JSON.stringify(request.body), this.sessionStorageService.getEncryptionKey()).pipe(mergeMap(encryptedContents => {
      return next.handle(request.clone({ body: encryptedContents, responseType: 'text' }));
    }))
  }

  private handshakeFilter(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (this.sessionStorageService.getSession() == undefined) {
      return next.handle(request.clone({ responseType: 'text' }));
    }
    return this.encrptionService.getRsaKeyPair().pipe(mergeMap(keyPair => {
      return this.encrptionService.convertRsaPublicKeyToBase64(keyPair.publicKey).pipe(mergeMap(base64PublicKey => {
        return this.encrptionService.encryptAesContentWithStringKey(base64PublicKey, this.sessionStorageService.getSession()).pipe(map(encryptedRsa => {
          return request.clone({ body: encryptedRsa, responseType: 'text' });
        }), mergeMap(changedRequest => {
          return next.handle(changedRequest).pipe(mergeMap(response => {
            if (response instanceof HttpResponse) {
              this.encrptionService.stopRsaKeyPairCreation();
              return this.encrptionService.decryptRsaContent(response.body, keyPair.privateKey)
                .pipe(map(decrytedString => {
                  const model: SessionDataModel = JSON.parse(decrytedString);
                  model.key = Buffer.from(model.key, 'base64').toString();
                  return model;
                }), map(model => {
                  return response.clone({ body: model });
                }));
            }
            return of(response);
          }));
        }));
      }));
    }));
  }
}
