import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Modal } from './modal';

describe('Modal', () => {
  let component: Modal;
  let fixture: ComponentFixture<Modal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Modal],
      providers: []
    })
    .compileComponents();

    fixture = TestBed.createComponent(Modal);
    fixture.componentRef.setInput('title', 'Crear petición');
    fixture.componentRef.setInput('primaryLabel', 'Crear petición');
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
