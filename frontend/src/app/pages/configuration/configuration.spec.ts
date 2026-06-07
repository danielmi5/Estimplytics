import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { ConfigurationPage } from './configuration';

describe('ConfigurationPage', () => {
  let component: ConfigurationPage;
  let fixture: ComponentFixture<ConfigurationPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConfigurationPage],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConfigurationPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
