package br.allandemiranda.fx.mapper;

import br.allandemiranda.fx.dto.MACDDto;
import br.allandemiranda.fx.model.MACD;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface MACDMapper {
    MACD toEntity(MACDDto MACDDto);

    MACDDto toDto(MACD MACD);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    MACD partialUpdate(MACDDto MACDDto, @MappingTarget MACD MACD);
}