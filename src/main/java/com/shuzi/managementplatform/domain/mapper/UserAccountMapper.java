package com.shuzi.managementplatform.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuzi.managementplatform.domain.entity.UserAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserAccountMapper extends BaseMapper<UserAccount> {
}
