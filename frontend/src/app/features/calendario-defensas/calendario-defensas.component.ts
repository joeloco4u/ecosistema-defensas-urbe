import { Component } from '@angular/core';
import { CalendarOptions } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';

@Component({
  selector: 'app-calendario-defensas',
  template: `
    <div class="space-y-6">
      <h2 class="text-2xl font-bold text-gray-800">Calendario de Defensas</h2>
      <div class="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
        <full-calendar [options]="calendarOptions"></full-calendar>
      </div>
    </div>
  `,
})
export class CalendarioDefensasComponent {
  calendarOptions: CalendarOptions = {
    plugins: [dayGridPlugin],
    initialView: 'dayGridMonth',
    weekends: true,
    locale: 'es',
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,dayGridWeek',
    },
    events: [],
  };
}
