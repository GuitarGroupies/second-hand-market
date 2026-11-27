package com.students.service;

import com.students.pojo.dto.ProductCreateDTO;
import com.students.pojo.dto.ProductUpdateDTO;
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
     * @param userId    当前用户 ID，可为 null（游客）
     * @return 商品详情
     */
    ProductDetailVO getDetail(Long productId, Long userId);

    /**
     * 发布商品。
     *
     * @param dto    发布入参
     * @param userId 发布者 ID
     * @return 新商品 ID
     */
    Long create(ProductCreateDTO dto, Long userId);
    /**
     * 修改商品（部分更新，字段为 null 表示不修改）。
     *
     * @param productId 商品 ID
     * @param dto       修改内容
     * @param userId    当前用户 ID
     */
    void update(Long productId, ProductUpdateDTO dto, Long userId);

    /**
     * 上架或下架商品。
     *
     * @param productId 商品 ID
     * @param status    目标状态：1 在售，0 下架
     * @param userId    当前用户 ID
     */
    void updateStatus(Long productId, Integer status, Long userId);

    /**
     * 删除商品（逻辑删除）。
     *
     * @param productId 商品 ID
     * @param userId    当前用户 ID
     */
    void delete(Long productId, Long userId);

    /**
     * 查询我发布的商品列表。
     *
     * @param query  查询条件
     * @param userId 当前用户 ID
     * @return 商品列表
     */
    List<ProductVO> listMine(ProductQuery query, Long userId);
}
