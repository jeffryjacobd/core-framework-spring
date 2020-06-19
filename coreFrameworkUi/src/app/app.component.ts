import { Component } from '@angular/core';
import { InputType } from './core/component/jcb-text-input/jcb-text-input.component';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  private id: String;
  private password: String;
  InputType = InputType;

  updateId(value: String): void {
    this.id = value;
  }

  updatePassword(value: String): void {
    this.password = value;
  }
}
