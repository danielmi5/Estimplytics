import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { RequestForm } from './request-form';
import { RequestsStateService } from '../../../core/requests/requests-state.service';

describe('RequestForm', () => {
  let component: RequestForm;
  let fixture: ComponentFixture<RequestForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RequestForm],
      providers: [provideHttpClient(), provideHttpClientTesting(), RequestsStateService]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RequestForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
