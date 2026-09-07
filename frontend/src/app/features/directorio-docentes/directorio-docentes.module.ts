import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { DirectorioDocentesComponent } from './directorio-docentes.component';
import { SharedModule } from '../../shared/shared.module';

const routes: Routes = [{ path: '', component: DirectorioDocentesComponent }];

@NgModule({
  declarations: [DirectorioDocentesComponent],
  imports: [CommonModule, FormsModule, SharedModule, RouterModule.forChild(routes)],
})
export class DirectorioDocentesModule {}