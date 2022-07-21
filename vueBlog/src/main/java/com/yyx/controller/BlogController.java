package com.yyx.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yyx.common.lang.Result;
import com.yyx.entity.Blog;
import com.yyx.service.BlogService;
import com.yyx.util.ShiroUtil;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author slamd14
 * @since 2022-07-19
 */
@RestController
@RequestMapping
public class BlogController {

    @Autowired
    BlogService blogService;

    @GetMapping("/blogs")
    public Result list(@RequestParam(defaultValue = "1") Integer currentPage){
        Page<Blog> blogPage = new Page<Blog>(currentPage,5);
        LambdaQueryWrapper<Blog> qw = new LambdaQueryWrapper<>();
        qw.orderByDesc(Blog::getCreated);
        blogService.page(blogPage, qw);
        return Result.succ(blogPage);
    }

    @GetMapping("/blogs/{id}")
    public Result detail(@PathVariable("id")Long id){
        Blog blog = blogService.getById(id);
        Assert.notNull(blog,"该博客已被删除");
        return Result.succ(blog);
    }

  //  @RequiresRoles(value = "admin") 接口进行 权限(AuthorizationInfo)过滤
    @RequiresAuthentication //接口进行 认证(AuthenticationInfo)过滤
    @PostMapping ("/blogs/edit")
    public Result edit(@Validated @RequestBody Blog blog){
        Blog temp=null;
        if(blog.getId()!=null){//编辑文章
            temp=blogService.getById(blog.getId());
            //当前登录用户只能编辑自己的文章
            Assert.isTrue(temp.getUserId().longValue()== ShiroUtil.getProfile().getId().longValue(),"没有权限编辑");
        }else {//新发布文章
            temp = new Blog();
            temp.setUserId(ShiroUtil.getProfile().getId());
            temp.setCreated(LocalDateTime.now());
            temp.setStatus(0);
        }
        BeanUtils.copyProperties(blog,temp,"id","userId","created","status");
        blogService.saveOrUpdate(temp);
        return Result.succ(null);
    }
}
