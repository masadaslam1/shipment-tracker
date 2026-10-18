import {inject, Injectable, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Shipment} from '../models/shipment.model';
import {Client} from "@stomp/stompjs";

export type ConnectionStatus = 'connecting' | 'connected' | 'disconnected';

@Injectable({providedIn: 'root'})
export class ShipmentService {
  private readonly http = inject(HttpClient);

  private readonly shipmentsSignal = signal<Shipment[]>([]);
  readonly shipments = this.shipmentsSignal.asReadonly();

  private readonly statusSignal = signal<ConnectionStatus>('connecting');
  readonly status = this.statusSignal.asReadonly();

  private readonly stompClient = new Client({
    brokerURL: 'ws://' + window.location.host + '/ws',
    reconnectDelay: 5000,
    onConnect: () => {
      this.statusSignal.set('connected');
      this.loadSnapshot();
    },
    onWebSocketClose: () => this.statusSignal.set('disconnected'),
  });

  constructor() {
    this.stompClient.activate();
    this.stompClient.subscribe('/topic/shipments', (message) => {
      this.handleFrame(JSON.parse(message.body) as Shipment);
    });
  }


  private handleFrame(frame: Shipment): void {
    const rest = this.shipmentsSignal()
      .filter((s) => s.trackingNumber !== frame.trackingNumber);
    rest.push(frame);
    this.shipmentsSignal.set(this.sortByLastUpdated(rest));
  }

  private loadSnapshot(): void {
    this.http.get<Shipment[]>('/api/shipments').subscribe({
      next: (shipments) => this.updateShipments(shipments),
      error: (err) => console.error('Failed to load shipment snapshot', err),
    });
  }


  private updateShipments(shipments: Shipment[]): void {
    const byTrackingNumber = new Map(
      this.shipmentsSignal().map((s) => [s.trackingNumber, s]),
    );
    for (const row of shipments) {
      const existing = byTrackingNumber.get(row.trackingNumber);
      if (!existing || Date.parse(row.lastUpdated) > Date.parse(existing.lastUpdated)) {
        byTrackingNumber.set(row.trackingNumber, row);
      }
    }
    this.shipmentsSignal.set(this.sortByLastUpdated([...byTrackingNumber.values()]));
  }

  private sortByLastUpdated(shipments: Shipment[]): Shipment[] {
    return [...shipments].sort(
      (a, b) => Date.parse(b.lastUpdated) - Date.parse(a.lastUpdated),
    );
  }
}
