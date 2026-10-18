import {Component, inject} from '@angular/core';
import {DatePipe} from '@angular/common';
import {ShipmentService} from '../../service/shipment.service';
import {ShipmentStatus} from '../../models/shipment.model';

@Component({
  selector: 'app-shipment-table',
  imports: [DatePipe],
  templateUrl: './shipment-table.component.html',
  styleUrl: './shipment-table.component.scss'
})
export class ShipmentTableComponent {
  private readonly shipmentService = inject(ShipmentService);
  readonly shipments = this.shipmentService.shipments;
  readonly status = this.shipmentService.status;

  private readonly statusColors: Record<ShipmentStatus, string> = {
    CREATED: 'secondary',
    IN_TRANSIT: 'primary',
    OUT_FOR_DELIVERY: 'info',
    DELIVERED: 'success',
    EXCEPTION: 'danger',
  };

  statusColor(status: ShipmentStatus): string {
    return this.statusColors[status];
  }

  statusLabel(status: ShipmentStatus): string {
    return status.replaceAll('_', ' ');
  }
}
