import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { RequestsPage } from './requests';

describe('RequestsPage', () => {
  let component: RequestsPage;
  let fixture: ComponentFixture<RequestsPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RequestsPage],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();

    fixture = TestBed.createComponent(RequestsPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});