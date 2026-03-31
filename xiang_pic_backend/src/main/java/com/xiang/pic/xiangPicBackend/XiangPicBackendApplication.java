package com.xiang.pic.xiangPicBackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.xiang.pic.xiangPicBackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true) // 开启 AOP
public class XiangPicBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiangPicBackendApplication.class, args);
    }

}
