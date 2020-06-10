package com.easybuy.goods.pojo;

import lombok.Data;

import java.util.List;

@Data
public class GoodsDto {
    private Spu spu;
    private List<Sku> skuList;
}
