import { describe, expect, it } from 'vitest';
import { isHighPriority, priorityTone } from './priority.util';

describe('priority.util', () => {
  it('maps high priorities', () => {
    expect(priorityTone('High')).toBe('high');
    expect(priorityTone('HIGH')).toBe('high');
    expect(isHighPriority('High')).toBe(true);
  });

  it('maps low priorities', () => {
    expect(priorityTone('Low')).toBe('low');
    expect(isHighPriority('Low')).toBe(false);
  });

  it('maps normal and medium as medium tone', () => {
    expect(priorityTone('Normal')).toBe('medium');
    expect(priorityTone('MEDIUM')).toBe('medium');
  });
});
