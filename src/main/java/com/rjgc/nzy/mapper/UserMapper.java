package com.rjgc.nzy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rjgc.nzy.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
