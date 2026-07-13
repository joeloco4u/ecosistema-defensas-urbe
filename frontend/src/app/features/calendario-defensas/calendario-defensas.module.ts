import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FullCalendarModule } from '@fullcalendar/angular';
import { CalendarioDefensasComponent } from './calendario-defensas.component';

const routes: Routes = [{ path: '', component: CalendarioDefensasComponent }];

@NgModule({
  declarations: [CalendarioDefensasComponent],
  imports: [CommonModule, FullCalendarModule, RouterModule.forChild(routes)],
})
export class CalendarioDefensasModule {}
