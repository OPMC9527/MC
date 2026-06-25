# MC 进销存管理系统

基于 Spring MVC + MyBatis 的简单进销存 Web 应用，包含商品档案、进货单、销售单和库存查询。

## 功能

- 商品档案：新增、修改、删除、查询；库存表中已有记录的商品禁止删除。
- 进货单：查询、新增；新增时自动生成 `yyyyMMdd` + 3 位流水号，自动写入当天日期，并累加或新增库存。
- 销售单：查询、新增；新增时自动生成 `yyyyMMdd` + 3 位流水号，校验库存存在且数量充足，保存后扣减库存。
- 库存管理：库存列表查询。
- 进货和销售数量在前端与服务端均限制为正数。

## 技术栈

- Spring MVC 5
- MyBatis + MyBatis-Spring
- H2 内存数据库（按题目表结构自动初始化）
- JSP + JSTL

## 运行

```bash
mvn package
```

将生成的 `target/inventory-mvc-1.0.0.war` 部署到 Servlet 容器（Tomcat 9 等）即可访问。
