import { Component, ChangeDetectionStrategy } from '@angular/core';
import { Activity } from '../../components/home/activity/activity';
import { Welcome } from '../../components/home/welcome/welcome';
import { MetricsSummary } from '../../components/home/kpis/metrics-summary';
import { Requests } from '../../components/home/requests/requests';
import { Stats } from '../../components/home/stats/stats';
import { ActivityItem, KpiCard, StatMetric, SummaryMetric } from './home.models';

@Component({
  selector: 'app-home',
  imports: [Activity, Welcome, MetricsSummary, Requests, Stats],
  templateUrl: './home.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HomePage {
  readonly kpis: KpiCard[] = [
    {
      id: 'kpi-requests',
      label: 'Peticiones activas',
      value: '18',
      change: '+12%',
      changeType: 'positive',
      icon: 'requests',
      description: 'Casos abiertos esperando análisis de impacto.'
    },
    {
      id: 'kpi-analysis',
      label: 'Análisis en curso',
      value: '7',
      change: '+3',
      changeType: 'neutral',
      icon: 'analysis',
      description: 'Estimaciones revisándose por el equipo funcional.'
    },
    {
      id: 'kpi-documents',
      label: 'Documentos generados',
      value: '124',
      change: '+8%',
      changeType: 'positive',
      icon: 'documents',
      description: 'Informes, resúmenes y entregables listos para validar.'
    }
  ];

  readonly activities: ActivityItem[] = [
    {
      id: 'activity-1',
      type: 'document',
      title: 'Informe de impacto RM-4521',
      description: 'Documento de análisis de impacto generado automaticamente.',
      user: 'Daniel',
      time: 'Hace 2 horas'
    },
    {
      id: 'activity-2',
      type: 'estimation',
      title: 'Estimación RM-4518',
      description: 'Estimación de esfuerzo: 40h desarrollo, 8h testing.',
      user: 'Daniel',
      time: 'Hace 5 horas'
    },
    {
      id: 'activity-3',
      type: 'analysis',
      title: 'Análisis de dependencias RM-4515',
      description: '3 dependencias críticas identificadas en el módulo de pagos.',
      user: 'Daniel',
      time: 'Hace 1 día'
    },
    {
      id: 'activity-4',
      type: 'analysis',
      title: 'Plan de migración RM-4521',
      description: 'Plan detallado con fases y rollback strategy.',
      user: 'Daniel',
      time: 'Hace 1 día'
    }
  ];

  readonly statMetrics: StatMetric[] = [
    {
      id: 'metric-1',
      label: 'Tiempo medio de análisis',
      value: 78,
      maxValue: 100,
      color: 'linear-gradient(90deg, #2f6bff 0%, #8cc0ff 100%)'
    },
    {
      id: 'metric-2',
      label: 'Cobertura documental',
      value: 92,
      maxValue: 100,
      color: 'linear-gradient(90deg, #0f9d7a 0%, #72d6b6 100%)'
    },
    {
      id: 'metric-3',
      label: 'Peticiones priorizadas',
      value: 64,
      maxValue: 100,
      color: 'linear-gradient(90deg, #ef7d32 0%, #f7b267 100%)'
    },
    {
      id: 'metric-4',
      label: 'Satisfacción del equipo',
      value: 88,
      maxValue: 100,
      color: 'linear-gradient(90deg, #6c63ff 0%, #8d89ff 100%)'
    }
  ];

  readonly summaryMetrics: SummaryMetric[] = [
    {
      id: 'summary-1',
      value: '1,247',
      label: 'Análisis completados',
      tone: 'success',
      icon: 'activity'
    },
    {
      id: 'summary-2',
      value: '98.5%',
      label: 'Uptime de la plataforma',
      tone: 'info',
      icon: 'info'
    },
    {
      id: 'summary-3',
      value: '3',
      label: 'Alertas activas',
      tone: 'warning',
      icon: 'alert-triangle'
    }
  ];
}