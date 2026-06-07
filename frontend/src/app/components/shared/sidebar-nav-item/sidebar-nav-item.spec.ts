import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { SidebarNavItem } from './sidebar-nav-item';

describe('SidebarNavItem', () => {
  let component: SidebarNavItem;
  let fixture: ComponentFixture<SidebarNavItem>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SidebarNavItem],
      providers: [provideRouter([])],
    })
    .compileComponents();

    fixture = TestBed.createComponent(SidebarNavItem);
    fixture.componentRef.setInput('label', 'Dashboard');
    fixture.componentRef.setInput('icon', 'home');
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
