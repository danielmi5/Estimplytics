import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { AnalysisPage } from './analysis';

describe('AnalysisPage', () => {
  let component: AnalysisPage;
  let fixture: ComponentFixture<AnalysisPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AnalysisPage],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(AnalysisPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
