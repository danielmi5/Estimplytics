export type PaginationItem =
  | { kind: 'page'; page: number }
  | { kind: 'separator' };

const page = (page: number): PaginationItem => ({ kind: 'page', page });
const separator = (): PaginationItem => ({ kind: 'separator' });

export function buildPaginationItems(currentPage: number, totalPages: number): PaginationItem[] {
  if (totalPages <= 5) {
    return Array.from({ length: totalPages }, (_, index) => page(index));
  }

  const lastPage = totalPages - 1;

  if (currentPage <= 2) {
    return [page(0), page(1), page(2), separator(), page(lastPage)];
  }

  if (currentPage >= totalPages - 3) {
    return [page(0), separator(), page(lastPage - 2), page(lastPage - 1), page(lastPage)];
  }

  return [
    page(0),
    separator(),
    page(currentPage - 1),
    page(currentPage),
    page(currentPage + 1),
    separator(),
    page(lastPage)
  ];
}
