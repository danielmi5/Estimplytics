export type HourType = 'hoursPlanning' | 'hoursAnalysis' | 'hoursDevelopment' | 'hoursTesting';

export const HOUR_TYPES: readonly HourType[] = [
  'hoursPlanning',
  'hoursAnalysis',
  'hoursDevelopment',
  'hoursTesting',
] as const;

export const HOUR_TYPE_LABELS: Record<HourType, string> = {
  hoursPlanning: 'Planificación',
  hoursAnalysis: 'Análisis',
  hoursDevelopment: 'Desarrollo',
  hoursTesting: 'Pruebas',
};

export type HourBreakdown = Record<HourType, number>;

export function sumHourBreakdown(breakdown: HourBreakdown): number {
  return HOUR_TYPES.reduce((total, type) => total + (breakdown[type] ?? 0), 0);
}
