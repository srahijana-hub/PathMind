package com.example.studyagent.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.studyagent.auth.entity.StudyUserDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudyUserMapper extends BaseMapper<StudyUserDO> {
}
