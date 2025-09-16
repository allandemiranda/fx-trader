package br.allandemiranda.fx.mapper;

import br.allandemiranda.fx.dto.RSIDto;
import br.allandemiranda.fx.model.RSI;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface RSIMapper {
    RSI toEntity(RSIDto RSIDto);

    RSIDto toDto(RSI RSI);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    RSI partialUpdate(RSIDto RSIDto, @MappingTarget RSI RSI);
}