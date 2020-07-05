import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { LoginComponent } from './core/security/login/login.component';
import { AuthGuard } from './core/security/auth/guard/auth.guard'


const routes: Routes = [
  {
    path: '',
    loadChildren: async () => (await import('./shared/shared.module')).SharedModule,
    canActivate: [AuthGuard]
  },
  {
    path: 'login',
    component: LoginComponent,
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
