package com.students.service;

import com.students.pojo.dto.ProductCreateDTO;
import com.students.pojo.query.ProductQuery;
import com.students.pojo.vo.ProductDetailVO;
import com.students.pojo.vo.ProductVO;

import java.util.List;

/**
 * 商品业务接口。
 *
 * @author students
 */
public interface ProductService {

    /**
     * 商品列表。
     *
     * @param query 查询条件
     * @return 商品列表
     */
    List<ProductVO> listProducts(ProductQuery query);

    /**
     * 商品详情。
     *
     * @param productId 商品 ID
     * @return 商品详情
     */
    ProductDetailVO getDetail(Long productId);

    /**
     * 发布商品。
     *
     * @param dto    发布入参
     * @param userId 发布者 ID
     * @return 新商品 ID
     */
    Long create(ProductCreateDTO dto, Long userId);
}
