import {TestBed} from '@angular/core/testing';
import {signal} from '@angular/core';

import {AppComponent} from './app.component';
import {ShipmentService} from './service/shipment.service';
import {Shipment} from './models/shipment.model';

describe('AppComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        {
          provide: ShipmentService,
          useValue: {
            shipments: signal<Shipment[]>([]),
            status: signal('connected' as const),
          },
        },
      ],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should render the shipment table', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('app-shipment-table')).toBeTruthy();
  });
});
