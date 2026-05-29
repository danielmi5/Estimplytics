import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserProfileForm } from './user-profile-form';

describe('UserProfileForm', () => {
  let component: UserProfileForm;
  let fixture: ComponentFixture<UserProfileForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserProfileForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserProfileForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
