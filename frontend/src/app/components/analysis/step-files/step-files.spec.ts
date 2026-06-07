import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StepFiles } from './step-files';

describe('StepFiles', () => {
  let component: StepFiles;
  let fixture: ComponentFixture<StepFiles>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StepFiles],
    }).compileComponents();

    fixture = TestBed.createComponent(StepFiles);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
