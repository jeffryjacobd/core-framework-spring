import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { SessionStorageService } from '../../session/service/session-storage.service';

@Injectable({
  providedIn: 'root'
})
export class AuthInterceptor implements HttpInterceptor {

  constructor(private sessionStorageService: SessionStorageService) { }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    (this.sessionStorageService.getSession() != undefined) && (request = request.clone({ setHeaders: { 'X-Auth-Token': this.sessionStorageService.getSession() } }));
    return next.handle(request);
  }
}
