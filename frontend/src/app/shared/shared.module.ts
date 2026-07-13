import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonComponent } from './components/button/button.component';
import { ModalComponent } from './components/modal/modal.component';
import { AlertComponent } from './components/alert/alert.component';

@NgModule({
  declarations: [ButtonComponent, ModalComponent, AlertComponent],
  imports: [CommonModule],
  exports: [ButtonComponent, ModalComponent, AlertComponent],
})
export class SharedModule {}
