import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { JcbButtonComponent } from './jcb-button/jcb-button.component';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { JcbTextInputComponent } from './jcb-text-input/jcb-text-input.component';



@NgModule({
  declarations: [JcbButtonComponent, JcbTextInputComponent],
  imports: [
    CommonModule,MatButtonModule,MatIconModule,MatFormFieldModule,MatInputModule
  ],
  exports: [JcbButtonComponent, JcbTextInputComponent]
})
export class ComponentModule { }
