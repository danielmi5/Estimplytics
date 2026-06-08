export type PriorityTone = 'high' | 'medium' | 'low';

export function priorityTone(priority?: string | null): PriorityTone {
  const value = priority?.trim().toLowerCase();

  if (value === 'high' || value === 'urgent') {
    return 'high';
  }

  if (value === 'low') {
    return 'low';
  }

  return 'medium';
}

export function isHighPriority(priority?: string | null): boolean {
  return priorityTone(priority) === 'high';
}
