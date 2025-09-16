package br.allandemiranda.fx.mapper;

import br.allandemiranda.fx.dto.EMAsDto;
import br.allandemiranda.fx.model.EMAs;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface EMAsMapper {
    EMAs toEntity(EMAsDto EMAsDto);

    EMAsDto toDto(EMAs EMAs);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    EMAs partialUpdate(EMAsDto EMAsDto, @MappingTarget EMAs EMAs);
}