import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StepAnalysis } from './step-analysis';

describe('StepAnalysis', () => {
  let component: StepAnalysis;
  let fixture: ComponentFixture<StepAnalysis>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StepAnalysis],
    }).compileComponents();

    fixture = TestBed.createComponent(StepAnalysis);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
