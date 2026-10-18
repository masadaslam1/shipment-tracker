import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {ShipmentTableComponent} from './components/shipment-table/shipment-table.component';

@Component({
  selector: 'app-root',
  imports: [ShipmentTableComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'frontend';
}
