package com.shuzi.managementplatform.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuzi.managementplatform.domain.entity.TrainingRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis mapper for training records.
 */
@Mapper
public interface TrainingRecordMapper extends BaseMapper<TrainingRecord> {
}
