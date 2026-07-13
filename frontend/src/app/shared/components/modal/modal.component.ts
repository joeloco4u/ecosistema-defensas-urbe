import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-modal',
  template: `
    <div *ngIf="visible" class="fixed inset-0 z-50 flex items-center justify-center">
      <div class="fixed inset-0 bg-black bg-opacity-50 transition-opacity" (click)="onClose.emit()"></div>
      <div class="relative bg-white rounded-xl shadow-2xl max-w-lg w-full mx-4 p-6 z-10">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold text-gray-900">{{ title }}</h3>
          <button (click)="onClose.emit()" class="text-gray-400 hover:text-gray-600">&times;</button>
        </div>
        <ng-content></ng-content>
      </div>
    </div>
  `,
})
export class ModalComponent {
  @Input() visible = false;
  @Input() title = '';
  @Output() onClose = new EventEmitter<void>();
}
