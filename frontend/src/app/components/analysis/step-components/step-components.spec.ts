import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StepComponents } from './step-components';

describe('StepComponents', () => {
  let component: StepComponents;
  let fixture: ComponentFixture<StepComponents>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StepComponents],
    }).compileComponents();

    fixture = TestBed.createComponent(StepComponents);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
