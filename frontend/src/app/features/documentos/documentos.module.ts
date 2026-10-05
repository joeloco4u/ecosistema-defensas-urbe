import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { DocumentosComponent } from './documentos.component';

const routes: Routes = [{ path: '', component: DocumentosComponent }];

@NgModule({
  declarations: [DocumentosComponent],
  imports: [CommonModule, FormsModule, RouterModule.forChild(routes)],
})
export class DocumentosModule {}
