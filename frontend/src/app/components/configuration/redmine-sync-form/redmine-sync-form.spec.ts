import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RedmineSyncForm } from './redmine-sync-form';

describe('RedmineSyncForm', () => {
  let component: RedmineSyncForm;
  let fixture: ComponentFixture<RedmineSyncForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RedmineSyncForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RedmineSyncForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
