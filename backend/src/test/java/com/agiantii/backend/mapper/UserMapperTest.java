package com.agiantii.backend.mapper;

import com.agiantii.backend.pojo.User;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@SpringBootTest(
        classes = {
                UserMapper.class,
                DataSourceAutoConfiguration.class,
                MybatisPlusAutoConfiguration.class,
                DataSource.class,
                SqlSessionFactory.class
        }
)
@MapperScan(basePackages = "com.agiantii.backend.mapper")
class UserMapperTest {
    @Resource
    private UserMapper userMapper;

    private static final int TEST_USER_ID = 99999;

    @AfterEach
    void cleanup() {
        userMapper.deleteUserNyId(TEST_USER_ID);
    }

    @Test
    void insertUser() {
        User user = new User();
        user.setId(TEST_USER_ID);
        user.setUsername("test_admin");
        user.setPassword("1234");
        user.setRole("admin");
        userMapper.insertUser(user);
        User saved = userMapper.selectUserById(TEST_USER_ID);
        Assertions.assertNotNull(saved);
        Assertions.assertEquals("test_admin", saved.getUsername());
        Assertions.assertEquals("admin", saved.getRole());
    }

    @Test
    void deleteUser() {
    }

    @Test
    void updateUser() {
    }

    @Test
    void selectUserByName() {
    }

    @Test
    void selectUserByRole() {
    }

    @Test
    void selectUserByIdAndPassword() {
    }
}