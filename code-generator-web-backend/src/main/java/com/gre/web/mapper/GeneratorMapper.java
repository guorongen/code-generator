package com.gre.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gre.web.model.entity.Generator;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Administrator
 * @description 针对表【generator(代码生成器)】的数据库操作Mapper
 * @createDate 2025-09-24 21:33:35
 * @Entity com.gre.web.model.entity.Generator
 */
public interface GeneratorMapper extends BaseMapper<Generator> {

    @Select("select id, distPath from generator where isDelete = 1")
    List<Generator> listDeletedGenerator();
}




