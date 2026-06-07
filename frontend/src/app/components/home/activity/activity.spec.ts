import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Activity } from './activity';

describe('Activity', () => {
  let component: Activity;
  let fixture: ComponentFixture<Activity>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Activity],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Activity);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('activities', []);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
