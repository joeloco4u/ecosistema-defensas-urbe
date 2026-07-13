import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { GestionProyectosComponent } from './gestion-proyectos.component';
import { SharedModule } from '../../shared/shared.module';

const routes: Routes = [{ path: '', component: GestionProyectosComponent }];

@NgModule({
  declarations: [GestionProyectosComponent],
  imports: [CommonModule, SharedModule, RouterModule.forChild(routes)],
})
export class GestionProyectosModule {}
