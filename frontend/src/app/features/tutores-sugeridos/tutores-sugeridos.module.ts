import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { TutoresSugeridosComponent } from './tutores-sugeridos.component';

const routes: Routes = [{ path: '', component: TutoresSugeridosComponent }];

@NgModule({
  declarations: [TutoresSugeridosComponent],
  imports: [CommonModule, FormsModule, RouterModule.forChild(routes)],
})
export class TutoresSugeridosModule {}
