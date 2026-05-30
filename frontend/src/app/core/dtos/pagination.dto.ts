export interface PageSort {
  sorted: boolean;
  unsorted: boolean;
  empty: boolean;
}

export interface PagePageable {
  pageNumber: number;
  pageSize: number;
  sort: PageSort;
  offset: number;
  paged: boolean;
  unpaged: boolean;
}

export interface Page<T> {
  content: T[];
  pageable: PagePageable;
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  size: number;
  number: number;
  numberOfElements: number;
  empty: boolean;
  sort: PageSort;
}

export interface PageParams {
  page?: number;
  size?: number;
  sort?: string;
  search?: string;
}
