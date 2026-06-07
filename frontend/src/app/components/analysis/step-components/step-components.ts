import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { FeatherIconDirective } from '../../../directives/feather-icon.directive';
import { Button } from '../../shared/button/button';
import { FormCheckbox } from '../../shared/form-checkbox/form-checkbox';
import type { StepState } from '../../../pages/analysis/step-state';
import type { ArchitecturalComponent, ComponentTreeNode } from '../analysis.models';

@Component({
  selector: 'app-step-components',
  imports: [Button, FeatherIconDirective, FormCheckbox],
  templateUrl: './step-components.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StepComponents {
  readonly state = input<StepState>('inactive');
  readonly components = input<ArchitecturalComponent[]>([]);
  readonly isLoading = input(false);
  readonly errorMessage = input<string | null>(null);
  readonly selectedIds = input<readonly string[]>([]);
  readonly selectedNames = input<readonly string[]>([]);

  readonly stepCompleted = output<readonly string[]>();
  readonly stepEdit = output<void>();
  readonly selectionChange = output<readonly string[]>();

  readonly completedSummary = computed(() => {
    const names = this.selectedNames();
    if (names.length === 0) {
      return 'Sin componentes seleccionados.';
    }
    return `Etiquetas seleccionadas: ${names.join(', ')}.`;
  });

  readonly tree = computed<ComponentTreeNode[]>(() => {
    const grouped = new Map<string, ArchitecturalComponent[]>();

    for (const component of this.components()) {
      const categoryComponents = grouped.get(component.category) ?? [];
      categoryComponents.push(component);
      grouped.set(component.category, categoryComponents);
    }

    return Array.from(grouped.entries()).map(([category, children]) => ({
      id: category,
      label: category,
      children,
    }));
  });

  readonly selectedTags = computed(() => {
    const names = this.selectedNames();
    const ids = this.selectedIds();
    return ids.map((id, index) => ({ id, name: names[index] ?? id }));
  });

  isSelected(componentId: string): boolean {
    return this.selectedIds().includes(componentId);
  }

  setComponentSelected(componentId: string, checked: boolean): void {
    const current = new Set(this.selectedIds());
    if (checked) {
      current.add(componentId);
    } else {
      current.delete(componentId);
    }
    this.selectionChange.emit(Array.from(current));
  }

  removeTag(componentId: string): void {
    const current = this.selectedIds().filter((id) => id !== componentId);
    this.selectionChange.emit(current);
  }

  continue(): void {
    this.stepCompleted.emit([...this.selectedIds()]);
  }

  onEdit(): void {
    this.stepEdit.emit();
  }
}
