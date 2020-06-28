import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { JcbCheckboxComponent } from './jcb-checkbox.component';

describe('JcbCheckboxComponent', () => {
  let component: JcbCheckboxComponent;
  let fixture: ComponentFixture<JcbCheckboxComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ JcbCheckboxComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JcbCheckboxComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
