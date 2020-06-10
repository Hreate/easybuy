package com.easybuy.goods.dao;

import com.easybuy.goods.pojo.Spec;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface SpecMapper extends Mapper<Spec> {
    @Select("select name,options from tb_spec where template_id in (select template_id from tb_category where name=#{categoryName})")
    List<Map> findSpecListByCategoryName(@Param("categoryName") String categoryName);
}
