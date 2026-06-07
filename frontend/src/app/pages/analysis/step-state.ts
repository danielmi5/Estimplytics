export type StepState = 'completed' | 'active' | 'inactive';
export type StepId = 'analysis' | 'components' | 'estimation' | 'files';

export const STEP_ORDER: readonly StepId[] = ['analysis', 'components', 'estimation', 'files'] as const;
