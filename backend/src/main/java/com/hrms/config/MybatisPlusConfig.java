package com.hrms.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MybatisPlusConfig implements MetaObjectHandler {

    private static final AtomicInteger sequence = new AtomicInteger(1000);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        // 自动生成工号: EMP + 日期 + 序号（填充到username）
        String employeeNo = generateEmployeeNo();
        this.strictInsertFill(metaObject, "username", String.class, employeeNo);

        // 入职日期默认今天
        this.strictInsertFill(metaObject, "entryDate", LocalDate.class, LocalDate.now());

        // 状态默认1(在职)
        this.strictInsertFill(metaObject, "status", Integer.class, 1);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    private String generateEmployeeNo() {
        String dateStr = LocalDateTime.now().format(formatter);
        int seq = sequence.getAndIncrement();
        return "EMP" + dateStr + String.format("%04d", seq % 10000);
    }
}
