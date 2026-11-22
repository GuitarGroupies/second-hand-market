package com.students.service;

import com.students.pojo.vo.CategoryTreeVO;

import java.util.List;

/**
 * 商品分类业务接口。
 *
 * @author students
 */
public interface CategoryService {

    /**
     * 查询完整的两级分类树。
     *
     * @return 一级分类列表，每个一级分类的 children 为其二级分类
     */
    List<CategoryTreeVO> listCategoryTree();
}
