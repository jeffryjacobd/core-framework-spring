import { Component, OnInit } from '@angular/core';
import { InputType } from '../../component/jcb-text-input/jcb-text-input.component';
import { ButtonIcon } from '../../component/jcb-button/jcb-button.component';
import { AuthService } from '../../security/auth/service/auth.service';
import { UserModel } from '../../security/auth/model/user-model'
import { Observable } from 'rxjs';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  private id: string;
  private password: string;
  private rememberMe: boolean;
  disableLoginButton: boolean;
  InputType = InputType;
  ButtonIcon = ButtonIcon;
  buttonIcon: ButtonIcon = undefined;
  isIdChangeEventEnabled: boolean = true;
  isPwdChangeEventEnabled: boolean = true;

  constructor(private authservice: AuthService) { }
  updateId(value: string): void {
    this.id = value;
    this.isIdChangeEventEnabled = false;
  }

  updatePassword(value: string): void {
    this.password = value;
    this.isPwdChangeEventEnabled = false;
  }

  isLocalStorage(state: boolean) {
    this.rememberMe = state;
  }
  loginButtonClicked(): void {
    this.buttonIcon = ButtonIcon.loading;
    this.disableLoginButton = true;
    const userModel: UserModel = { user: this.id || '', password: this.password || '', rememberMe: this.rememberMe || false };
    this.authservice.login(userModel).subscribe();
  }

  ngOnInit(): void {
    this.disableLoginButton = false;
  }
}
