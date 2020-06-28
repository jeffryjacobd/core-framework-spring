import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'jcb-checkbox',
  templateUrl: './jcb-checkbox.component.html',
  styleUrls: ['./jcb-checkbox.component.css']
})
export class JcbCheckboxComponent implements OnInit {

  @Input() name: string;
  @Input() color?: string;
  @Input() isChecked: boolean = false;
  @Input() id?: number;
  @Output() checked: EventEmitter<boolean>;
  checkbox: JcbCheckBox;
  constructor() {
    this.checked = new EventEmitter<boolean>();
  }

  ngOnInit(): void {
  }

  change(state: boolean) {
    this.checked.emit(state);
  }
}

export class JcbCheckBox {
  id: number;
  name: string;
  checkedState: boolean = false;
  color: string = 'primary';
}