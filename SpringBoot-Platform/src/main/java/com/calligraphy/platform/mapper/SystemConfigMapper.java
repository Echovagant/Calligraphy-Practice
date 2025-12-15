package com.calligraphy.platform.mapper;

import com.calligraphy.platform.dto.SystemConfigDto;
import com.calligraphy.platform.entity.SystemConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SystemConfigMapper {
    
    SystemConfigDto toDto(SystemConfig systemConfig);
    
    SystemConfig toEntity(SystemConfigDto systemConfigDto);
}


