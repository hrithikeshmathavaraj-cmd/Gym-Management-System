package com.gymmanagement.mapper;

import com.gymmanagement.dto.MemberRequest;
import com.gymmanagement.dto.MemberResponse;
import com.gymmanagement.entity.Member;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper converting between the Member entity and its request/response DTOs.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MemberMapper {

    Member toEntity(MemberRequest request);

    MemberResponse toResponse(Member member);

    void updateEntityFromRequest(MemberRequest request, @MappingTarget Member member);
}
