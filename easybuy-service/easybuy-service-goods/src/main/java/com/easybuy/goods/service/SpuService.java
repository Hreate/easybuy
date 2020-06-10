package com.easybuy.goods.service;

import com.easybuy.goods.pojo.GoodsDto;
import com.easybuy.goods.pojo.Spu;
import com.github.pagehelper.Page;

import java.util.List;
import java.util.Map;

public interface SpuService {

    /***
     * 查询所有
     * @return
     */
    List<Spu> findAll();

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    Spu findById(String id);

    /***
     * 新增
     * @param goodsDto
     */
    void add(GoodsDto goodsDto);

    /***
     * 修改
     * @param goodsDto
     */
    void update(GoodsDto goodsDto);

    /***
     * 删除
     * @param id
     */
    void delete(String id);

    /***
     * 多条件搜索
     * @param searchMap
     * @return
     */
    List<Spu> findList(Map<String, Object> searchMap);

    /***
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    Page<Spu> findPage(int page, int size);

    /***
     * 多条件分页查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    Page<Spu> findPage(Map<String, Object> searchMap, int page, int size);

    /**
     * 查询
     * @param id
     * @return
     */
    GoodsDto findGoodsById(String id);

    /**
     * 商品审核并自动上架
     * @param id
     */
    void audit(String id);

    /**
     * 下架商品
     * @param id
     */
    void pull(String id);

    /**
     * 商品上架
     * @param id
     */
    void put(String id);

    /**
     * 还原商品
     * @param id
     */
    void restore(String id);

    /**
     * 物理删除商品
     * @param id
     */
    void realDel(String id);
}
