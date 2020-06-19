import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginComponent } from './login/login.component';
import { HomeComponent } from './home/home.component';
import { CoreModule } from '../core/core.module';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'home',
    component: HomeComponent,
  },
  {
    path: '',
    component: LoginComponent,
  }
];

@NgModule({
  declarations: [LoginComponent, HomeComponent],
  imports: [
    CommonModule,
    CoreModule,
    RouterModule.forChild(routes)
  ]
})
export class SharedModule { }
