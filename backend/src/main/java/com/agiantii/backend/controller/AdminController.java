package com.agiantii.backend.controller;


import com.agiantii.backend.common.Constant;
import com.agiantii.backend.common.R;
import com.agiantii.backend.mapper.AdminMapper;
import com.agiantii.backend.mapper.UserMapper;
import com.agiantii.backend.pojo.Admin;
import com.agiantii.backend.pojo.User;
import com.agiantii.backend.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
// 已废弃 旧版管理员控制器 已从Spring容器移除 管理功能迁移到新架构
// @RestController
// @RequestMapping("/admin")
public class AdminController {
    @Resource
    private AdminMapper adminMapper;
    @Resource
    private UserMapper userMapper;
    @PostMapping("/register")
    R<String> register(@RequestBody Admin admin, @RequestParam(value = "password",defaultValue = "123") String password){
        log.info("insert admin admin:{} password:{}",admin.toString(),password);
        User user = new User();
        user.setRole(Constant.ROLE_ADMIN);
        user.setPassword(password);
        user.setId(admin.getAdminId());
        user.setUsername("admin_" + admin.getAdminId());

        try{
            adminMapper.insertAdmin(admin);
            userMapper.insertUser(user);
            return R.success(Constant.REGISTER_SUCCESS);
        }
        catch (Exception e){
            return R.error(Constant.REGISTER_FAIL);
        }
    }
    @GetMapping("deleteByAdminId")
    R<String> deleteByAdminId(@RequestParam(value = "adminId") Integer adminId){
        log.info("delete admin adminId:{}",adminId);
        try{
            adminMapper.deleteByAdminId(adminId);
            return R.success("delete success");
        }
        catch (Exception e){
            return R.error("delete fail");
        }
    }

    @PostMapping("updateAdmin")
    R<String> updateByAdminId(@RequestParam(value = "adminId") Integer adminId, @RequestBody Admin admin){
        log.info("update admin adminId:{} admin:{}",adminId,admin.toString());
        try{
            adminMapper.updateAdmin(admin);
            return R.success("update success");
        }
        catch (Exception e){
            return R.error("update fail");
        }
    }

    @GetMapping("selectByAdminName")
    R<List<Admin>> selectByAdminName(@RequestParam(value = "adminName") String adminName){
        log.info("select admin adminName:{}",adminName);
        try{
            List<Admin> adminList;
            if(StringUtil.isEmpty(adminName)){
                adminList = adminMapper.selectByName(adminName);
            }
            else{
                adminList = adminMapper.selectAll();
            }
            return R.success(adminList,"查询成功");
        }
        catch (Exception e){
            return R.error("查询失败");
        }
    }
}
