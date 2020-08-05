import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'jcb-text-input',
  templateUrl: './jcb-text-input.component.html',
  styleUrls: ['./jcb-text-input.component.css']
})
export class JcbTextInputComponent implements OnInit {
  @Input() label: String;
  @Input() inputType: InputType;
  @Input() isChangeEventEnabled: boolean;
  @Output() valueEvent = new EventEmitter<String>();
  constructor() {
  }
  ngOnInit(): void {
    (!this.inputType) && (this.inputType = InputType.Text);
    (this.isChangeEventEnabled == undefined) && (this.isChangeEventEnabled = false)
  }
  update(value: String): void {
    this.valueEvent.emit(value);
  }
}

export enum InputType {
  Date = "date",
  Email = "email",
  Number = "number",
  Password = "password",
  Text = "text",
  Phone = "tel"
}