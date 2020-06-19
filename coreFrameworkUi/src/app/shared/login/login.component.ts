import { Component, OnInit } from '@angular/core';
import { InputType } from '../../core/component/jcb-text-input/jcb-text-input.component';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  private id: String;
  private password: String;
  InputType = InputType;

  updateId(value: String): void {
    this.id = value;
  }

  updatePassword(value: String): void {
    this.password = value;
  }
  ngOnInit(): void {
  }
}
