import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthInterceptor } from './auth/interceptor/auth.interceptor';
import { AuthService } from './auth/service/auth.service';



@NgModule({
  declarations: [AuthInterceptor, AuthService],
  imports: [
    CommonModule
  ]
})
export class SecurityModule { }
