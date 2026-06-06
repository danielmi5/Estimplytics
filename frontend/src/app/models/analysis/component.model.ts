import type { ComponentResponse } from '../../core/components/component.dto';

export type ArchitecturalComponent = ComponentResponse;

export interface ComponentTreeNode {
  id: string;
  label: string;
  children: ArchitecturalComponent[];
}
