import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { JcbButtonComponent } from './jcb-button.component';

describe('JcbButtonComponent', () => {
  let component: JcbButtonComponent;
  let fixture: ComponentFixture<JcbButtonComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ JcbButtonComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JcbButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
