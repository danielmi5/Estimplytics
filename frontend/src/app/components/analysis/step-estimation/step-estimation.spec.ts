import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StepEstimation } from './step-estimation';

describe('StepEstimation', () => {
  let component: StepEstimation;
  let fixture: ComponentFixture<StepEstimation>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StepEstimation],
    }).compileComponents();

    fixture = TestBed.createComponent(StepEstimation);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
