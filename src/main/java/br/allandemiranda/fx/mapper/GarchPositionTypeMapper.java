package br.allandemiranda.fx.mapper;

import br.allandemiranda.fx.dto.GarchPositionTypeDto;
import br.allandemiranda.fx.model.embeddable.GarchPositionType;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface GarchPositionTypeMapper {
    GarchPositionType toEntity(GarchPositionTypeDto garchPositionTypeDto);

    GarchPositionTypeDto toDto(GarchPositionType garchPositionType);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    GarchPositionType partialUpdate(GarchPositionTypeDto garchPositionTypeDto, @MappingTarget GarchPositionType garchPositionType);
}