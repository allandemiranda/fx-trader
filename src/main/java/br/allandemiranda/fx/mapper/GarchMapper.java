package br.allandemiranda.fx.mapper;

import br.allandemiranda.fx.dto.GarchDto;
import br.allandemiranda.fx.model.Garch;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface GarchMapper {
    Garch toEntity(GarchDto garchDto);

    GarchDto toDto(Garch garch);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Garch partialUpdate(GarchDto garchDto, @MappingTarget Garch garch);
}