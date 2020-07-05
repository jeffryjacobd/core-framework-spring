import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginComponent } from './login/login.component';
import { ComponentModule } from '../component/component.module';
//import { StorageServiceModule } from 'angular-webstorage-service';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { AuthInterceptor } from './auth/interceptor/auth.interceptor'
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
      useClass: AuthInterceptor,
      multi: true,
    }, AuthService, AuthGuard, SessionStorageService]
})
export class SecurityModule { }
