import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { CalendarioDefensasComponent } from './calendario-defensas.component';
import { ModalAgendamientoComponent } from '../../shared/components/modal-agendamiento/modal-agendamiento.component';

const routes: Routes = [{ path: '', component: CalendarioDefensasComponent }];

@NgModule({
  declarations: [CalendarioDefensasComponent],
  imports: [CommonModule, FormsModule, ModalAgendamientoComponent, RouterModule.forChild(routes)],
})
export class CalendarioDefensasModule {}
