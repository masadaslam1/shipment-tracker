package dev.masadaslam1.shipment_tracker.mapper;

import dev.masadaslam1.shipment_tracker.domain.Shipment;
import dev.masadaslam1.shipment_tracker.dto.ShipmentDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ShipmentMapper {

    ShipmentDto toDto(Shipment shipment);

    List<ShipmentDto> toDtoList(List<Shipment> shipments);


}
