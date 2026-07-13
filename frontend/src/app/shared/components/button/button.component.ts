import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-button',
  template: `
    <button
      [type]="type"
      [disabled]="disabled"
      (click)="onClick.emit($event)"
      [ngClass]="buttonClasses"
      class="inline-flex items-center justify-center px-4 py-2 rounded-lg font-medium transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2">
      <ng-content></ng-content>
    </button>
  `,
})
export class ButtonComponent {
  @Input() type: 'button' | 'submit' = 'button';
  @Input() variant: 'primary' | 'secondary' | 'danger' | 'ghost' = 'primary';
  @Input() disabled = false;
  @Output() onClick = new EventEmitter<MouseEvent>();

  get buttonClasses(): Record<string, boolean> {
    return {
      'bg-primary-600 text-white hover:bg-primary-700 focus:ring-primary-500': this.variant === 'primary',
      'bg-gray-200 text-gray-700 hover:bg-gray-300 focus:ring-gray-400': this.variant === 'secondary',
      'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500': this.variant === 'danger',
      'bg-transparent text-gray-600 hover:bg-gray-100 focus:ring-gray-400': this.variant === 'ghost',
      'opacity-50 cursor-not-allowed': this.disabled,
    };
  }
}
