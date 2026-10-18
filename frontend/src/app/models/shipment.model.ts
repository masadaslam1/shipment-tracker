export type ShipmentStatus =
  | 'CREATED'
  | 'IN_TRANSIT'
  | 'OUT_FOR_DELIVERY'
  | 'DELIVERED'
  | 'EXCEPTION';

export interface Shipment {
  id: number;
  trackingNumber: string;
  status: ShipmentStatus;
  origin: string;
  destination: string;
  currentLocation: string | null;
  estimatedDelivery: string;
  actualDelivery: string | null;
  weight: number;
  carrier: string | null;
  description: string | null;
  createdAt: string;
  lastUpdated: string;
}
