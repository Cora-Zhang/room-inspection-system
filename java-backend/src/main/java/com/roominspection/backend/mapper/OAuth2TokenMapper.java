package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.OAuth2Token;
import org.apache.ibatis.annotations.Mapper;

/**
 * OAuth2.0令牌Mapper
 */
@Mapper
public interface OAuth2TokenMapper extends BaseMapper<OAuth2Token> {
}
