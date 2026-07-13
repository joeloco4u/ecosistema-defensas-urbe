import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalAgendamientoComponent } from './modal-agendamiento.component';

describe('ModalAgendamientoComponent', () => {
  let component: ModalAgendamientoComponent;
  let fixture: ComponentFixture<ModalAgendamientoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalAgendamientoComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ModalAgendamientoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
