package com.easybuy.goods.dao;

import com.easybuy.goods.pojo.Brand;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface BrandMapper extends Mapper<Brand> {
    @Select("select name,image from tb_brand where id in(select brand_id from tb_category_brand where category_id in (select id from tb_category where name=#{categoryName}))")
    List<Brand> findBrandListByCategoryName(@Param("categoryName") String categoryName);
}
