import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Pagination } from './pagination';

describe('Pagination', () => {
  let component: Pagination;
  let fixture: ComponentFixture<Pagination>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Pagination],
      providers: [provideRouter([])]
    }).compileComponents();

    fixture = TestBed.createComponent(Pagination);
    fixture.componentRef.setInput('page', 0);
    fixture.componentRef.setInput('pageSize', 10);
    fixture.componentRef.setInput('totalElements', 25);
    fixture.componentRef.setInput('totalPages', 3);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
