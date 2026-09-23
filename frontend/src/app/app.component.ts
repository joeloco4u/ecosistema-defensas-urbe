import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-root',
  template: `
    <div class="flex min-h-screen bg-background-dark">
      <app-sidebar *ngIf="!isLoginPage"></app-sidebar>
      <div class="flex-1 flex flex-col min-w-0">
        <app-navbar *ngIf="!isLoginPage"></app-navbar>
        <main class="flex-1 overflow-auto min-w-0 w-full" [ngClass]="isLoginPage ? '' : 'p-6'">
          <router-outlet></router-outlet>
        </main>
      </div>
    </div>
  `,
})
export class AppComponent {
  title = 'Ecosistema Defensas URBE';

  constructor(private router: Router) {}

  get isLoginPage(): boolean {
    return this.router.url === '/login';
  }
}
