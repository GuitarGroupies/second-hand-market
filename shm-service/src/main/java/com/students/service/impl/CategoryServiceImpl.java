package com.students.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.students.mapper.ProductCategoryMapper;
import com.students.pojo.entity.ProductCategory;
import com.students.pojo.vo.CategoryTreeVO;
import com.students.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品分类业务实现。
 *
 * <p><b>实现思路（避免 N+1 查询）：</b>分类一共只有几十条，一次查全表拿到所有分类，
 * 在内存里按 parentId 分组组装成树。如果按「先查一级，再逐个查二级」的方式，
 * 22 个一级分类就要发 23 次 SQL，这就是典型的 N+1 问题。</p>
 *
 * @author students
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final ProductCategoryMapper productCategoryMapper;

    @Override
    public List<CategoryTreeVO> listCategoryTree() {
        // 一次性查出全部分类，按 sort 升序
        List<ProductCategory> allList = productCategoryMapper.selectList(
                new LambdaQueryWrapper<ProductCategory>()
                        .orderByAsc(ProductCategory::getSort));
        log.info("查询分类总数：{}", allList.size());

        if (allList.isEmpty()) {
            // 返回空集合而不是 null，调用方无需判空
            return Collections.emptyList();
        }

        // 按 parentId 分组：key 是父 ID，value 是其子分类
        Map<Long, List<ProductCategory>> childrenMap = allList.stream()
                .filter(category -> category.getParentId() != null && category.getParentId() > 0)
                .collect(Collectors.groupingBy(ProductCategory::getParentId));

        // 遍历一级分类，把子分类挂到 children 上
        List<CategoryTreeVO> result = new ArrayList<>(16);
        for (ProductCategory category : allList) {
            if (category.getParentId() == null || category.getParentId() != 0L) {
                continue;
            }
            CategoryTreeVO parent = convert(category);
            List<ProductCategory> children = childrenMap.getOrDefault(category.getId(), Collections.emptyList());
            parent.setChildren(children.stream().map(this::convert).collect(Collectors.toList()));
            result.add(parent);
        }
        return result;
    }

    /**
     * 实体转 VO，只拷贝前端需要的字段。
     *
     * @param entity 分类实体
     * @return 分类树节点
     */
    private CategoryTreeVO convert(ProductCategory entity) {
        CategoryTreeVO vo = new CategoryTreeVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setIcon(entity.getIcon());
        vo.setChildren(new ArrayList<>(0));
        return vo;
    }
}
