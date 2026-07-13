-- MySQL dump 10.13  Distrib 8.0.46, for Linux (aarch64)
--
-- Host: localhost    Database: edu_platform
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `edu_platform`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `edu_platform` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `edu_platform`;

--
-- Table structure for table `tb_assignment`
--

DROP TABLE IF EXISTS `tb_assignment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_assignment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL,
  `teacher_id` bigint NOT NULL,
  `title` varchar(100) NOT NULL,
  `description` text,
  `full_score` int NOT NULL,
  `deadline` datetime NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_assignment_course` (`course_id`),
  KEY `idx_assignment_teacher` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_assignment`
--

LOCK TABLES `tb_assignment` WRITE;
/*!40000 ALTER TABLE `tb_assignment` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_assignment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_course`
--

DROP TABLE IF EXISTS `tb_course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_course` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `teacher_id` bigint NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` text,
  `cover_url` varchar(255) DEFAULT NULL,
  `max_students` int NOT NULL DEFAULT '100',
  `enrolled_count` int NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_course_teacher` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_course`
--

LOCK TABLES `tb_course` WRITE;
/*!40000 ALTER TABLE `tb_course` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_course` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_course_selection`
--

DROP TABLE IF EXISTS `tb_course_selection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_course_selection` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `status` tinyint NOT NULL COMMENT '0=PENDING, 1=APPROVED, 2=DROPPED, 4=REJECTED',
  `applied_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `reviewed_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_selection_course_student` (`course_id`,`student_id`),
  KEY `idx_selection_student` (`student_id`),
  KEY `idx_selection_course_status` (`course_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_course_selection`
--

LOCK TABLES `tb_course_selection` WRITE;
/*!40000 ALTER TABLE `tb_course_selection` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_course_selection` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_discussion`
--

DROP TABLE IF EXISTS `tb_discussion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_discussion` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'è®¨è®ºID',
  `course_id` bigint NOT NULL COMMENT 'è¯¾ç¨‹IDï¼Œå¯¹åº” tb_course.id',
  `parent_id` bigint DEFAULT NULL COMMENT 'çˆ¶è®¨è®ºIDï¼ŒNULLè¡¨ç¤ºä¸»é¢˜å¸–',
  `author_id` bigint NOT NULL COMMENT 'ä½œè€…IDï¼Œå¯¹åº” tb_user.id',
  `title` varchar(100) DEFAULT NULL COMMENT 'ä¸»é¢˜å¸–æ ‡é¢˜ï¼Œå›žå¤å¯ä¸ºç©º',
  `content` text NOT NULL COMMENT 'è®¨è®ºå†…å®¹',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT 'çŠ¶æ€ï¼š0=éšè—ï¼Œ1=æ­£å¸¸',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤ï¼š0=æ­£å¸¸ï¼Œ1=åˆ é™¤',
  PRIMARY KEY (`id`),
  KEY `idx_discussion_course_parent` (`course_id`,`parent_id`),
  KEY `idx_discussion_author` (`author_id`),
  KEY `idx_discussion_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='è¯¾ç¨‹è®¨è®ºè¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_discussion`
--

LOCK TABLES `tb_discussion` WRITE;
/*!40000 ALTER TABLE `tb_discussion` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_discussion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_enrollment`
--

DROP TABLE IF EXISTS `tb_enrollment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_enrollment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'é€‰è¯¾è®°å½•ID',
  `course_id` bigint NOT NULL COMMENT 'è¯¾ç¨‹IDï¼Œå¯¹åº” tb_course.id',
  `student_id` bigint NOT NULL COMMENT 'å­¦ç”ŸIDï¼Œå¯¹åº” tb_user.id',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0=PENDING, 1=APPROVED, 2=DROPPED, 4=REJECTED',
  `apply_reason` varchar(255) DEFAULT NULL COMMENT 'é€‰è¯¾ç”³è¯·è¯´æ˜Ž',
  `review_comment` varchar(255) DEFAULT NULL COMMENT 'å®¡æ ¸æ„è§',
  `applied_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'ç”³è¯·æ—¶é—´',
  `reviewed_at` datetime DEFAULT NULL COMMENT 'å®¡æ ¸æ—¶é—´',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_enrollment_course_student` (`course_id`,`student_id`),
  KEY `idx_enrollment_student` (`student_id`),
  KEY `idx_enrollment_course_status` (`course_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='å­¦ç”Ÿé€‰è¯¾è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_enrollment`
--

LOCK TABLES `tb_enrollment` WRITE;
/*!40000 ALTER TABLE `tb_enrollment` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_enrollment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_exam`
--

DROP TABLE IF EXISTS `tb_exam`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_exam` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'è€ƒè¯•ID',
  `course_id` bigint NOT NULL COMMENT 'è¯¾ç¨‹IDï¼Œå¯¹åº” tb_course.id',
  `teacher_id` bigint NOT NULL COMMENT 'å‘å¸ƒæ•™å¸ˆIDï¼Œå¯¹åº” tb_user.id',
  `title` varchar(100) NOT NULL COMMENT 'è€ƒè¯•æ ‡é¢˜',
  `description` text COMMENT 'è€ƒè¯•è¯´æ˜Ž',
  `start_time` datetime NOT NULL COMMENT 'å¼€å§‹æ—¶é—´',
  `end_time` datetime NOT NULL COMMENT 'ç»“æŸæ—¶é—´',
  `full_score` int NOT NULL DEFAULT '100' COMMENT 'æ»¡åˆ†',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT 'è€ƒè¯•çŠ¶æ€ï¼š0=è‰ç¨¿ï¼Œ1=å·²å‘å¸ƒï¼Œ2=å·²ç»“æŸ',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT 'é€»è¾‘åˆ é™¤ï¼š0=æ­£å¸¸ï¼Œ1=åˆ é™¤',
  PRIMARY KEY (`id`),
  KEY `idx_exam_course` (`course_id`),
  KEY `idx_exam_teacher` (`teacher_id`),
  KEY `idx_exam_time` (`start_time`,`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='è€ƒè¯•è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_exam`
--

LOCK TABLES `tb_exam` WRITE;
/*!40000 ALTER TABLE `tb_exam` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_exam` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_forum_post`
--

DROP TABLE IF EXISTS `tb_forum_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_forum_post` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL,
  `author_id` bigint NOT NULL,
  `title` varchar(100) NOT NULL,
  `content` text NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_post_course` (`course_id`),
  KEY `idx_post_author` (`author_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_forum_post`
--

LOCK TABLES `tb_forum_post` WRITE;
/*!40000 ALTER TABLE `tb_forum_post` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_forum_post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_forum_reply`
--

DROP TABLE IF EXISTS `tb_forum_reply`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_forum_reply` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `author_id` bigint NOT NULL,
  `content` text NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_reply_post` (`post_id`),
  KEY `idx_reply_author` (`author_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_forum_reply`
--

LOCK TABLES `tb_forum_reply` WRITE;
/*!40000 ALTER TABLE `tb_forum_reply` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_forum_reply` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_submission`
--

DROP TABLE IF EXISTS `tb_submission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_submission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assignment_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `content` text NOT NULL,
  `score` int DEFAULT NULL,
  `comment` text,
  `submitted_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `graded_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_submission_assignment_student` (`assignment_id`,`student_id`),
  KEY `idx_submission_student` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_submission`
--

LOCK TABLES `tb_submission` WRITE;
/*!40000 ALTER TABLE `tb_submission` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_submission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tb_user`
--

DROP TABLE IF EXISTS `tb_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(100) NOT NULL,
  `nickname` varchar(50) NOT NULL,
  `role` tinyint NOT NULL COMMENT '1=STUDENT, 2=TEACHER, 3=ADMIN',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username_deleted` (`username`,`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_user`
--

LOCK TABLES `tb_user` WRITE;
/*!40000 ALTER TABLE `tb_user` DISABLE KEYS */;
INSERT INTO `tb_user` VALUES (1,'admin','$2a$10$iIX0pZ3BeDoxhJLNAFivnObkFFAA90lNSOc9p1wx8lxF65QBVmoYu','系统管理员',3,'2026-07-11 11:18:47','2026-07-11 11:18:47',0);
/*!40000 ALTER TABLE `tb_user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-11 11:40:08
