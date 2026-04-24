package com.archer.task.mapper;

import com.archer.task.entity.News;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface NewsMapper  extends BaseMapper<News> {

    @Select("select count(*) from news where url=#{url} and is_delete = 'N'")
    int getCountByUrl(@Param("url") String url);

}
