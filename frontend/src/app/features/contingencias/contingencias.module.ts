import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ContingenciasComponent } from './contingencias.component';

const routes: Routes = [{ path: '', component: ContingenciasComponent }];

@NgModule({
  declarations: [ContingenciasComponent],
  imports: [CommonModule, RouterModule.forChild(routes)],
})
export class ContingenciasModule {}
