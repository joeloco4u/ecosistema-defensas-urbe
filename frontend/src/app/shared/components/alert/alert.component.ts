import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-alert',
  template: `
    <div *ngIf="visible" [ngClass]="alertClasses" class="flex items-center p-4 rounded-lg border" role="alert">
      <span class="text-sm font-medium">{{ message }}</span>
      <button *ngIf="dismissible" (click)="visible = false" class="ml-auto">&times;</button>
    </div>
  `,
})
export class AlertComponent {
  @Input() type: 'success' | 'error' | 'warning' | 'info' = 'info';
  @Input() message = '';
  @Input() dismissible = true;
  visible = true;

  get alertClasses(): Record<string, boolean> {
    return {
      'bg-green-50 border-green-200 text-green-800': this.type === 'success',
      'bg-red-50 border-red-200 text-red-800': this.type === 'error',
      'bg-yellow-50 border-yellow-200 text-yellow-800': this.type === 'warning',
      'bg-blue-50 border-blue-200 text-blue-800': this.type === 'info',
    };
  }
}
