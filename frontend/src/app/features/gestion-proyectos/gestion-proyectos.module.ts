import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { GestionProyectosComponent } from './gestion-proyectos.component';
import { SharedModule } from '../../shared/shared.module';
import { ModalAgendamientoComponent } from '../../shared/components/modal-agendamiento/modal-agendamiento.component';

const routes: Routes = [{ path: '', component: GestionProyectosComponent }];

@NgModule({
  declarations: [GestionProyectosComponent],
  imports: [CommonModule, FormsModule, SharedModule, ModalAgendamientoComponent, RouterModule.forChild(routes)],
})
export class GestionProyectosModule {}
