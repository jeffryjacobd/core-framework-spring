import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { JcbTextInputComponent } from './jcb-text-input.component';

describe('JcbTextInputComponent', () => {
  let component: JcbTextInputComponent;
  let fixture: ComponentFixture<JcbTextInputComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ JcbTextInputComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JcbTextInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
