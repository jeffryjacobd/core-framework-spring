import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { JcbButtonComponent } from './jcb-button/jcb-button.component';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { JcbTextInputComponent } from './jcb-text-input/jcb-text-input.component';
import { JcbCheckboxComponent } from './jcb-checkbox/jcb-checkbox.component';



@NgModule({
  declarations: [JcbButtonComponent, JcbTextInputComponent, JcbCheckboxComponent],
  imports: [
    CommonModule,MatButtonModule,MatIconModule,MatFormFieldModule,MatInputModule,MatCheckboxModule
  ],
  exports: [JcbButtonComponent, JcbTextInputComponent,JcbCheckboxComponent]
})
export class ComponentModule { }
