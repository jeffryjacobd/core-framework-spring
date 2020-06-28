import { Component, OnInit } from '@angular/core';
import { InputType } from '../../core/component/jcb-text-input/jcb-text-input.component';
import { ButtonIcon } from 'src/app/core/component/jcb-button/jcb-button.component';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  private id: String;
  private password: String;
  private rememberMe: boolean;
  disableLoginButton: boolean;
  InputType = InputType;
  ButtonIcon = ButtonIcon;
  buttonIcon: ButtonIcon = undefined;
  updateId(value: String): void {
    this.id = value;
  }

  updatePassword(value: String): void {
    this.password = value;
  }

  isLocalStorage(state: boolean) {
    this.rememberMe = state;
  }
  loginButtonClicked() {
    this.buttonIcon = ButtonIcon.loading;
    this.disableLoginButton = true;
  }

  ngOnInit(): void {
    this.disableLoginButton = false;
  }
}
