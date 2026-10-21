package com.calligraphy.platform.mapper;

import com.calligraphy.platform.dto.DiscussionDto;
import com.calligraphy.platform.model.Discussion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * 讨论实体 → DTO 的映射。
 *
 * <p>只有 {@code toDto} 一个方法：列表端点用 {@code Page.map(mapper::toDto)}，
 * 不需要单独的 List 版本。
 *
 * <p>{@code replies} 显式 ignore —— 回复由服务层按状态过滤后单独塞进去
 * （实体的 replies 集合不带状态过滤，会把已删除的回复也带出来）。
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface DiscussionMapper {

    /**
     * 实体主键是 Long，契约里主键字段是字符串 {@code id}，MapStruct 会转成字符串。
     * {@code isPinned}/{@code isFeatured} 显式声明，名字对不上就编译失败，
     * 而不是悄悄映射成 null。
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "isPinned", source = "isPinned")
    @Mapping(target = "isFeatured", source = "isFeatured")
    @Mapping(target = "replies", ignore = true)
    DiscussionDto toDto(Discussion discussion);
}
