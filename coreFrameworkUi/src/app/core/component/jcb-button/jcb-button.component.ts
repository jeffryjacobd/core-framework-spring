import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'jcb-button',
  templateUrl: './jcb-button.component.html',
  styleUrls: ['./jcb-button.component.css']
})
export class JcbButtonComponent implements OnInit {

  @Input() buttonType : ButtonType;
  @Input() buttonColor : ButtonColor;
  @Input() disabled : Boolean;
  @Input() buttonIcon : ButtonIcon = undefined ;
  readonly ButtonType = ButtonType;
  readonly ButtonColor = ButtonColor;
  @Input() text : String;
  constructor() {
   }

  ngOnInit(): void {
    !this.buttonType && (this.buttonType = ButtonType.Raise);
    !this.text && (this.text = 'Login');
    !this.buttonColor && (this.buttonColor = ButtonColor.Primary);
    !this.disabled && (this.disabled= false);
  }


}

export enum ButtonType {
  Basic,
  Raise,
  Flat,
  Fab,
  MiniFab
}

export enum ButtonColor {
Primary = 'primary',
Accent = 'accent',
Warn = 'warn',
Basic = 'no-color'
}

export enum ButtonIcon {

}