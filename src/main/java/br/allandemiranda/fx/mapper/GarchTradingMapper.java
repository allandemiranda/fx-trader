package br.allandemiranda.fx.mapper;

import br.allandemiranda.fx.dto.GarchTradingDto;
import br.allandemiranda.fx.model.GarchTrading;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {GarchPositionTypeMapper.class})
public interface GarchTradingMapper {
    GarchTrading toEntity(GarchTradingDto garchTradingDto);

    GarchTradingDto toDto(GarchTrading garchTrading);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    GarchTrading partialUpdate(GarchTradingDto garchTradingDto, @MappingTarget GarchTrading garchTrading);
}