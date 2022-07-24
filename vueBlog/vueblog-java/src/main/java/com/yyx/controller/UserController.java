package com.yyx.controller;


import com.yyx.common.lang.Result;
import com.yyx.entity.User;
import com.yyx.service.UserService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @RequiresAuthentication
    @GetMapping("/{id}")
    public Result getById(@PathVariable("id")Long id){
        User user = userService.getById(id);
        if(user==null){
            return Result.fail("查询不到id对应的用户");
        }
        return Result.succ(user);
    }

    @PostMapping("/save")
    public Result save(@Validated @RequestBody User user){
        return Result.succ(user);
    }
}
