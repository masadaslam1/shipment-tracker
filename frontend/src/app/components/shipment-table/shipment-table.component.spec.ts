import {ComponentFixture, TestBed} from '@angular/core/testing';
import {signal} from '@angular/core';

import {ShipmentTableComponent} from './shipment-table.component';
import {ShipmentService} from '../../service/shipment.service';
import {Shipment} from '../../models/shipment.model';

describe('ShipmentTableComponent', () => {
  let component: ShipmentTableComponent;
  let fixture: ComponentFixture<ShipmentTableComponent>;

  const sampleShipment: Shipment = {
    id: 1,
    trackingNumber: 'TRK-1000',
    status: 'IN_TRANSIT',
    origin: 'Shanghai',
    destination: 'Rotterdam',
    currentLocation: 'Singapore',
    estimatedDelivery: '2026-11-01T10:00:00Z',
    actualDelivery: null,
    weight: 12.5,
    carrier: 'Maersk',
    description: 'demo shipment',
    createdAt: '2026-10-18T10:00:00Z',
    lastUpdated: '2026-10-18T11:00:00Z',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ShipmentTableComponent],
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

    fixture = TestBed.createComponent(ShipmentTableComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show the Live badge when connected', () => {
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.badge')?.textContent).toContain('Live');
  });

  it('should render one row per shipment with status label', () => {
    const service = TestBed.inject(ShipmentService) as unknown as { shipments: ReturnType<typeof signal<Shipment[]>> };
    service.shipments.set([sampleShipment]);

    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;

    const rows = compiled.querySelectorAll('tbody tr');
    expect(rows.length).toBe(1);
    expect(rows[0].textContent).toContain('TRK-1000');
    expect(rows[0].textContent).toContain('IN TRANSIT');
    expect(rows[0].textContent).toContain('Shanghai → Rotterdam');
  });
});
