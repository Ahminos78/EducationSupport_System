# AI_CONTEXT.md

# 在线教育辅助教学系统（AI协作上下文）

## 项目简介

本项目为大学集中实训项目。

项目名称：

在线教育辅助教学系统

开发周期：

21天

开发方式：

多人协作开发。

---

## 项目目标

本项目不仅需要完成传统在线教育业务，还需要融合AI能力，实现智能辅助教学。

最终目标：

建立一个基于 Spring Cloud Alibaba 的微服务在线教育平台，并集成 AI 教学辅助能力。

项目最终可用于：

- 集中实训验收
- 校招项目展示
- 简历项目

---

## 技术路线

后端：

- Java 21
- Spring Boot 3
- Spring Cloud Alibaba
- MyBatis Plus
- MySQL
- Redis

微服务：

- Gateway
- Nacos
- OpenFeign

前端：

- Vue3
- Element Plus
- Axios
- Pinia

AI：

- Spring AI
- DeepSeek API
- RAG
- Qdrant（后期）

---

## 当前开发阶段

Version：

v0.1

当前目标：

完成整个SpringCloud Alibaba微服务框架。

暂未开始AI开发。

---

## 当前确定的业务模块

传统业务：

- 用户管理
- 登录认证
- 课程管理
- 学生选课
- 作业管理
- 考试管理
- 成绩统计
- 论坛讨论

AI业务：

- 智能课程问答（RAG）
- AI作业评语
- 学习进度预警
- 知识点摘要
- 智能组卷

---

## 当前项目原则

1.

先完成微服务框架。

2.

先完成传统业务。

3.

最后接入AI。

4.

所有AI能力统一放入 edu-ai 服务。

5.

优先保证项目稳定，再增加AI能力。

---

## AI协作要求

当AI生成代码时：

- 保持代码可维护
- 保持模块独立
- 尽量遵循DDD思想
- 不要过度设计
- 不提前实现未来需求