import { buildPaginationItems } from './pagination-items.util';

describe('buildPaginationItems', () => {
  it('should return all pages when total is five or fewer', () => {
    expect(buildPaginationItems(0, 5)).toEqual([
      { kind: 'page', page: 0 },
      { kind: 'page', page: 1 },
      { kind: 'page', page: 2 },
      { kind: 'page', page: 3 },
      { kind: 'page', page: 4 }
    ]);
  });

  it('should show the first three pages and the last one at the start', () => {
    expect(buildPaginationItems(0, 11)).toEqual([
      { kind: 'page', page: 0 },
      { kind: 'page', page: 1 },
      { kind: 'page', page: 2 },
      { kind: 'separator' },
      { kind: 'page', page: 10 }
    ]);
  });

  it('should keep the start pattern while the current page is within the first three', () => {
    expect(buildPaginationItems(2, 11)).toEqual([
      { kind: 'page', page: 0 },
      { kind: 'page', page: 1 },
      { kind: 'page', page: 2 },
      { kind: 'separator' },
      { kind: 'page', page: 10 }
    ]);
  });

  it('should show the current page with neighbours in the middle', () => {
    expect(buildPaginationItems(5, 11)).toEqual([
      { kind: 'page', page: 0 },
      { kind: 'separator' },
      { kind: 'page', page: 4 },
      { kind: 'page', page: 5 },
      { kind: 'page', page: 6 },
      { kind: 'separator' },
      { kind: 'page', page: 10 }
    ]);
  });

  it('should show the last three pages and the first one at the end', () => {
    expect(buildPaginationItems(10, 11)).toEqual([
      { kind: 'page', page: 0 },
      { kind: 'separator' },
      { kind: 'page', page: 8 },
      { kind: 'page', page: 9 },
      { kind: 'page', page: 10 }
    ]);
  });
});
