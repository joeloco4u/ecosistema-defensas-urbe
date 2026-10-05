import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { JuradosSugeridosComponent } from './jurados-sugeridos.component';

const routes: Routes = [{ path: '', component: JuradosSugeridosComponent }];

@NgModule({
  declarations: [JuradosSugeridosComponent],
  imports: [CommonModule, FormsModule, RouterModule.forChild(routes)],
})
export class JuradosSugeridosModule {}
