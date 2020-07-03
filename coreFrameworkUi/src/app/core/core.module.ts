import { NgModule } from '@angular/core';
import { ComponentModule } from './component/component.module';
import { SecurityModule } from './security/security.module';



@NgModule({
  declarations: [],
  imports: [
    ComponentModule, SecurityModule
  ],
  exports: [ComponentModule, SecurityModule]
})
export class CoreModule { }
