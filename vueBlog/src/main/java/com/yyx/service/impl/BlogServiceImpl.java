package com.yyx.service.impl;

import com.yyx.entity.Blog;
import com.yyx.mapper.BlogMapper;
import com.yyx.service.BlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author slamd14
 * @since 2022-07-19
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

}
