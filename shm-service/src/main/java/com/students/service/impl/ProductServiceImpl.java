package com.students.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.students.common.constant.RedisKeyConstant;
import com.students.common.exception.BizException;
import com.students.common.result.ResultCode;
import com.students.mapper.ProductImageMapper;
import com.students.mapper.ProductLikeMapper;
import com.students.mapper.ProductMapper;
import com.students.mapper.ProductWantMapper;
import com.students.mapper.SysUserMapper;
import com.students.pojo.dto.ProductCreateDTO;
import com.students.pojo.dto.ProductUpdateDTO;
import com.students.pojo.entity.Product;
import com.students.pojo.entity.ProductImage;
import com.students.pojo.entity.ProductLike;
import com.students.pojo.entity.ProductWant;
import com.students.pojo.entity.SysUser;
import com.students.pojo.query.ProductQuery;
import com.students.pojo.vo.ProductDetailVO;
import com.students.pojo.vo.ProductVO;
import com.students.service.ProductService;
import com.students.service.ViewCountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 商品业务实现。
 *
 * @author students
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    /** 商品状态：在售 */
    private static final int STATUS_ON_SALE = 1;

    /** 排序方式：最新发布 */
    private static final String SORT_NEW = "new";

    /** 排序方式：价格升序 */
    private static final String SORT_PRICE_ASC = "price_asc";

    /** 排序方式：价格降序 */
    private static final String SORT_PRICE_DESC = "price_desc";

    /** 标签数量上限 */
    private static final int MAX_TAG_COUNT = 10;

    /** 空值缓存过期时间（分钟），比正常缓存短很多 */
    private static final long NULL_CACHE_TTL_MINUTES = 5L;

    /** 缓存过期随机偏移上限（秒），打散过期时间防止雪崩 */
    private static final int TTL_RANDOM_SECONDS = 300;

    private final ProductMapper productMapper;

    private final ProductImageMapper productImageMapper;

    private final SysUserMapper sysUserMapper;

    private final ProductLikeMapper productLikeMapper;

    private final ProductWantMapper productWantMapper;

    private final ViewCountService viewCountService;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public List<ProductVO> listProducts(ProductQuery query) {
        // 1. 构造分页对象（页码与条数都经过边界保护）
        Page<Product> page = new Page<>(query.getSafePageNum(), query.getSafePageSize());

        // 2. 构造查询条件
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        // 只查在售商品
        wrapper.eq(Product::getStatus, STATUS_ON_SALE);
        // 分类筛选：categoryId 为 null 时该条件整体跳过
        wrapper.eq(query.getCategoryId() != null, Product::getCategoryId, query.getCategoryId());
        // 关键词筛选：必须同时判 null 与空白串
        wrapper.like(StringUtils.hasText(query.getKeyword()), Product::getTitle, query.getKeyword());
        // 价格区间
        wrapper.ge(query.getMinPrice() != null, Product::getPrice, query.getMinPrice());
        wrapper.le(query.getMaxPrice() != null, Product::getPrice, query.getMaxPrice());

        // 3. 排序：只接受白名单中的值，防止 SQL 注入
        applySort(wrapper, query.getSortBy());

        // 4. 分页查询（会自动执行一条 COUNT 查询）
        IPage<Product> result = productMapper.selectPage(page, wrapper);
        log.info("商品列表查询：pageNum={}, pageSize={}, total={}",
                result.getCurrent(), result.getSize(), result.getTotal());

        // 5. 实体转 VO
        return result.getRecords().stream()
                .map(this::convertToProductVO)
                .toList();
    }

    @Override
    public ProductDetailVO getDetail(Long productId, Long userId) {
        // 0. 浏览量 +1：只写 Redis，由定时任务批量落库
        viewCountService.increment(productId);

        // 1. 先查缓存
        String cacheKey = RedisKeyConstant.PRODUCT_DETAIL + productId;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);

        // 2. 缓存命中
        if (cached != null) {
            // 空值标记：说明数据库里也没有，直接返回商品不存在
            if (RedisKeyConstant.CACHE_NULL.equals(cached)) {
                throw new BizException(ResultCode.PRODUCT_NOT_FOUND);
            }
            log.info("商品详情命中缓存：productId={}", productId);
            ProductDetailVO cachedVo = JSONUtil.toBean(cached, ProductDetailVO.class);
            // 缓存里不含「当前用户」相关状态，需要实时补上
            fillUserState(cachedVo, userId);
            return cachedVo;
        }

        // 3. 缓存未命中，查数据库
        Product product = productMapper.selectById(productId);
        if (product == null) {
            // 防穿透：把"不存在"也缓存起来，短时间内直接拦截重复请求
            stringRedisTemplate.opsForValue().set(cacheKey, RedisKeyConstant.CACHE_NULL,
                    Duration.ofMinutes(NULL_CACHE_TTL_MINUTES));
            throw new BizException(ResultCode.PRODUCT_NOT_FOUND);
        }

        // 4. 组装详情（商品 + 图片 + 卖家昵称）
        ProductDetailVO detail = buildDetail(product);
        // 点赞/想要总数随商品走，可以进缓存；当前用户状态放在缓存之外补
        detail.setLikeCount(productLikeMapper.selectCount(
                new LambdaQueryWrapper<ProductLike>()
                        .eq(ProductLike::getProductId, productId)));
        detail.setWantCount(productWantMapper.selectCount(
                new LambdaQueryWrapper<ProductWant>()
                        .eq(ProductWant::getProductId, productId)));
        fillUserState(detail, userId);

        // 5. 写回缓存：基础 TTL + 随机偏移，防雪崩
        long ttl = RedisKeyConstant.PRODUCT_DETAIL_TTL_MINUTES * 60
                + ThreadLocalRandom.current().nextInt(TTL_RANDOM_SECONDS);
        stringRedisTemplate.opsForValue()
                .set(cacheKey, JSONUtil.toJsonStr(detail), Duration.ofSeconds(ttl));
        log.info("商品详情写入缓存：productId={}, ttl={}s", productId, ttl);

        return detail;
    }

    /**
     * 组装商品详情：商品 + 图片 + 卖家昵称。
     *
     * @param product 商品实体
     * @return 详情 VO
     */
    private ProductDetailVO buildDetail(Product product) {
        ProductDetailVO vo = new ProductDetailVO();
        vo.setId(product.getId());
        vo.setSellerId(product.getSellerId());
        vo.setCategoryId(product.getCategoryId());
        vo.setTitle(product.getTitle());
        vo.setDescription(product.getDescription());
        vo.setPrice(product.getPrice());
        vo.setOriginalPrice(product.getOriginalPrice());
        vo.setStock(product.getStock());
        vo.setViewCount(product.getViewCount());
        vo.setStatus(product.getStatus());
        vo.setCreateTime(product.getCreateTime());
        vo.setTags(splitTags(product.getTags()));

        // 查图片，按 sort 升序
        List<ProductImage> images = productImageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, product.getId())
                        .orderByAsc(ProductImage::getSort));
        vo.setImages(images.stream().map(ProductImage::getUrl).toList());

        // 查卖家昵称，卖家可能已注销，兜底空串
        SysUser seller = sysUserMapper.selectById(product.getSellerId());
        vo.setSellerNickname(seller == null ? "" : seller.getNickname());

        return vo;
    }
    /**
     * 填充「当前用户」相关的互动状态。
     *
     * <p>这部分数据因人而异，不能进缓存，必须每次实时查询。
     * 游客（userId 为 null）时置为 false，前端据此隐藏高亮。</p>
     *
     * @param vo     商品详情
     * @param userId 当前用户 ID，可为 null
     */
    private void fillUserState(ProductDetailVO vo, Long userId) {
        if (userId == null) {
            vo.setLiked(false);
            vo.setWanted(false);
            return;
        }
        vo.setLiked(productLikeMapper.selectCount(
                new LambdaQueryWrapper<ProductLike>()
                        .eq(ProductLike::getUserId, userId)
                        .eq(ProductLike::getProductId, vo.getId())) > 0);
        vo.setWanted(productWantMapper.selectCount(
                new LambdaQueryWrapper<ProductWant>()
                        .eq(ProductWant::getUserId, userId)
                        .eq(ProductWant::getProductId, vo.getId())) > 0);
    }

    /**
     * 应用排序规则，使用白名单避免 SQL 注入。
     *
     * @param wrapper 查询条件
     * @param sortBy  排序方式
     */
    private void applySort(LambdaQueryWrapper<Product> wrapper, String sortBy) {
        String sort = sortBy == null ? SORT_NEW : sortBy;
        switch (sort) {
            case SORT_PRICE_ASC -> wrapper.orderByAsc(Product::getPrice);
            case SORT_PRICE_DESC -> wrapper.orderByDesc(Product::getPrice);
            case SORT_NEW -> wrapper.orderByDesc(Product::getCreateTime);
            default -> wrapper.orderByDesc(Product::getCreateTime);
        }
    }

    /**
     * 实体转列表 VO。
     *
     * @param entity 商品实体
     * @return 列表卡片 VO
     */
    private ProductVO convertToProductVO(Product entity) {
        ProductVO vo = new ProductVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setCoverImage(entity.getCoverImage());
        vo.setPrice(entity.getPrice());
        vo.setOriginalPrice(entity.getOriginalPrice());
        vo.setCategoryId(entity.getCategoryId());
        vo.setViewCount(entity.getViewCount());
        vo.setCreateTime(entity.getCreateTime());
        vo.setTags(splitTags(entity.getTags()));
        return vo;
    }
    


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ProductCreateDTO dto, Long userId) {
        // 1. 插入商品主表
        Product product = new Product();
        product.setSellerId(userId);
        product.setCategoryId(dto.getCategoryId());
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription() == null ? "" : dto.getDescription());
        // 第一张图作为封面
        product.setCoverImage(dto.getImages() == null || dto.getImages().isEmpty()
                ? "" : dto.getImages().get(0));
        product.setPrice(dto.getPrice());
        product.setOriginalPrice(dto.getOriginalPrice() == null
                ? BigDecimal.ZERO : dto.getOriginalPrice());
        product.setStock(dto.getStock() == null ? 1 : dto.getStock());
        product.setViewCount(0);
        product.setStatus(STATUS_ON_SALE);
        product.setTags(String.join(",", cleanTags(dto.getTags())));

        productMapper.insert(product);
        // MyBatis-Plus 插入后会回填自增主键
        Long productId = product.getId();

        // 2. 插入图片，sort 决定展示顺序
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            for (int i = 0; i < dto.getImages().size(); i++) {
                ProductImage image = new ProductImage();
                image.setProductId(productId);
                image.setUrl(dto.getImages().get(i));
                image.setSort(i);
                productImageMapper.insert(image);
            }
        }

        // 3. 清理可能存在的旧缓存
        stringRedisTemplate.delete(RedisKeyConstant.PRODUCT_DETAIL + productId);

        log.info("商品发布成功：productId={}, sellerId={}, images={}",
                productId, userId, dto.getImages() == null ? 0 : dto.getImages().size());
        return productId;
    }
@Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long productId, ProductUpdateDTO dto, Long userId) {
        Product product = getOwnedProduct(productId, userId);

        // 部分更新：只有传了才改
        if (dto.getTitle() != null) {
            product.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription());
        }
        if (dto.getPrice() != null) {
            product.setPrice(dto.getPrice());
        }
        if (dto.getOriginalPrice() != null) {
            product.setOriginalPrice(dto.getOriginalPrice());
        }
        if (dto.getStock() != null) {
            product.setStock(dto.getStock());
        }
        if (dto.getCategoryId() != null) {
            product.setCategoryId(dto.getCategoryId());
        }
        if (dto.getTags() != null) {
            product.setTags(String.join(",", cleanTags(dto.getTags())));
        }
        // 图片：null 不处理；空数组表示清空；非空数组表示全量替换
        if (dto.getImages() != null) {
            List<String> urls = dto.getImages();
            product.setCoverImage(urls.isEmpty() ? "" : urls.get(0));
            productImageMapper.delete(new LambdaQueryWrapper<ProductImage>()
                    .eq(ProductImage::getProductId, productId));
            for (int i = 0; i < urls.size(); i++) {
                ProductImage image = new ProductImage();
                image.setProductId(productId);
                image.setUrl(urls.get(i));
                image.setSort(i);
                productImageMapper.insert(image);
            }
        }
        productMapper.updateById(product);
        // 写操作后必须清详情缓存，否则用户看到的是旧数据
        stringRedisTemplate.delete(RedisKeyConstant.PRODUCT_DETAIL + productId);
        log.info("商品修改成功：productId={}, userId={}", productId, userId);
    }

    @Override
    public void updateStatus(Long productId, Integer status, Long userId) {
        if (status == null || (status != 0 && status != 1 && status != 2)) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    "状态只能为 0(下架) / 1(在售) / 2(已售出)");
        }
        Product product = getOwnedProduct(productId, userId);
        product.setStatus(status);
        productMapper.updateById(product);
        stringRedisTemplate.delete(RedisKeyConstant.PRODUCT_DETAIL + productId);
        log.info("商品状态变更：productId={}, status={}, userId={}", productId, status, userId);
    }

    @Override
    public void delete(Long productId, Long userId) {
        getOwnedProduct(productId, userId);
        // 逻辑删除：@TableLogic 会改写为 UPDATE ... SET deleted = 1
        productMapper.deleteById(productId);
        stringRedisTemplate.delete(RedisKeyConstant.PRODUCT_DETAIL + productId);
        log.info("商品删除成功：productId={}, userId={}", productId, userId);
    }

    @Override
    public List<ProductVO> listMine(ProductQuery query, Long userId) {
        Page<Product> page = new Page<>(query.getSafePageNum(), query.getSafePageSize());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        // 核心：只查自己的商品，走 idx_seller 索引
        wrapper.eq(Product::getSellerId, userId);
        applySort(wrapper, query.getSortBy());
        IPage<Product> result = productMapper.selectPage(page, wrapper);
        log.info("我的发布查询：userId={}, total={}", userId, result.getTotal());
        return result.getRecords().stream()
                .map(this::convertToProductVO)
                .toList();
    }

    /**
     * 取出商品并校验归属，防止水平越权。
     *
     * <p><b>为什么必须校验：</b>只判断商品存在是不够的——
     * 否则任何登录用户都能修改、下架、删除别人的商品。</p>
     *
     * <p>注意用 equals 而不是 ==：Long 是包装类型，超出 -128~127 后
     * == 比较的是对象地址，会导致「明明是自己却报越权」。</p>
     *
     * @param productId 商品 ID
     * @param userId    当前用户 ID
     * @return 商品实体
     */
    private Product getOwnedProduct(Long productId, Long userId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BizException(ResultCode.PRODUCT_NOT_FOUND);
        }
        if (!product.getSellerId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN.getCode(), "只能操作自己发布的商品");
        }
        return product;
    }

    /**
     * 清洗标签：去掉 # 与首尾空格，过滤空值、含逗号值、超长值，去重并限制数量。
     *
     * <p><b>为什么必须过滤逗号：</b>存储格式是逗号分隔，
     * 若标签本身含逗号，拼进字符串后会被误解析成两个标签。</p>
     *
     * @param rawTags 原始标签
     * @return 清洗后的标签，永不为 null
     */
    private List<String> cleanTags(List<String> rawTags) {
        if (rawTags == null || rawTags.isEmpty()) {
            return Collections.emptyList();
        }
        return rawTags.stream()
                .filter(Objects::nonNull)
                .map(tag -> tag.replace("#", "").trim())
                .filter(tag -> !tag.isEmpty())
                .filter(tag -> !tag.contains(","))
                .filter(tag -> tag.length() <= 20)
                .distinct()
                .limit(MAX_TAG_COUNT)
                .collect(Collectors.toList());
    }

    /**
     * 把逗号分隔的标签串拆成列表。
     *
     * @param tags 存储的标签串，可能为 null 或空
     * @return 标签列表，永不为 null
     */
    private List<String> splitTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }}
