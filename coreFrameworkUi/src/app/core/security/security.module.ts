import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginComponent } from './login/login.component';
import { ComponentModule } from '../component/component.module';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { SessionInterceptor } from './session/interceptor/session.interceptor'
import { EncryptionInterceptor } from './encryption/interceptor/encryption.interceptor'
import { AuthService } from './auth/service/auth.service';
import { AuthGuard } from './auth/guard/auth.guard';
import { SessionStorageService } from './session/service/session-storage.service';


@NgModule({
  declarations: [LoginComponent],
  imports: [
    CommonModule, ComponentModule
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: SessionInterceptor,
      multi: true,
    }, {
      provide: HTTP_INTERCEPTORS,
      useClass: EncryptionInterceptor,
      multi: true,
    }, AuthService, AuthGuard, SessionStorageService]
})
export class SecurityModule { }
