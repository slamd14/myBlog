package com.yyx.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yyx.common.dto.LoginDto;
import com.yyx.common.lang.Result;
import com.yyx.entity.User;
import com.yyx.service.UserService;
import com.yyx.util.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@RestController
public class AccountController {

    @Autowired
    UserService userService;
    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public Result login(@Validated @RequestBody LoginDto loginDto, HttpServletResponse response){
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.eq(User::getUsername,loginDto.getUsername());
        User user = userService.getOne(qw);
        Assert.notNull(user,"用户不存在");
        //关于这里为什么不用shiro自带的认证--->这个项目里shiro整合了JWT，不是原生token，JWT中是没有前端传入的密码的
        if(!user.getPassword().equals(SecureUtil.md5(loginDto.getPassword()))){
            return Result.fail("密码不正确");
        }
        String jwt = jwtUtils.generateToken(user.getId());
        response.setHeader("Authorization",jwt);//TODO 把jwt放在响应头里返回，客户端收到响应头了会缓存jwt到cookie里吗--->应该是由前端处理，而不是浏览器自发帮我们做这件事(笑哭)
        response.setHeader("Access-control-Expose-Headers","Authorization");
        //return Result.succ(null);
        log.info("用户{}登录成功!",loginDto.getUsername());//TODO debug
        return Result.succ(MapUtil.builder()
                .put("id",user.getId())
                .put("username",user.getUsername())
                .put("avatar",user.getAvatar())
                .put("email",user.getEmail())
                .map()
        );
    }

    @RequiresAuthentication
    @GetMapping("/logout")
    public Result logout(){
        SecurityUtils.getSubject().logout();
        return Result.succ(null);
    }

    @PostMapping("/register")
    public Result register(@RequestBody LoginDto loginDto){
        String username = loginDto.getUsername().trim();//去掉字符串两侧的空格
        String password = loginDto.getPassword().trim();
        String md5Pass = SecureUtil.md5(password);
        User user = new User();
        user.setUsername(username);
        user.setPassword(md5Pass);
        user.setCreated(LocalDateTime.now());
        user.setAvatar("https://image-1300566513.cos.ap-guangzhou.myqcloud.com/upload/images/5a9f48118166308daba8b6da7e466aab.jpg");
        user.setStatus(0);
        userService.save(user);
        return Result.succ(null);
    }

    @RequiresAuthentication
    @PostMapping("/editPersonalMes")
    public Result editPersonalMes(@RequestBody User user){
        User oldUser = userService.getById(user.getId());
        if((user.getPassword()!=null)){
            user.setPassword(SecureUtil.md5(user.getPassword()));
        }
        BeanUtils.copyProperties(user,oldUser);
        userService.updateById(oldUser);
        return Result.succ(null);
    }
}

