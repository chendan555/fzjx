-- MySQL dump 10.13  Distrib 5.7.40, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: big_sample_db
-- ------------------------------------------------------
-- Server version	5.7.40

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `big_sample_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `big_sample_db` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_bin */;

USE `big_sample_db`;

--
-- Table structure for table `big_sample`
--

DROP TABLE IF EXISTS `big_sample`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `big_sample` (
  `id` varchar(11) COLLATE utf8_bin NOT NULL,
  `name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `experiment_design_id` varchar(11) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `big_sample`
--

LOCK TABLES `big_sample` WRITE;
/*!40000 ALTER TABLE `big_sample` DISABLE KEYS */;
INSERT INTO `big_sample` VALUES ('1','样本推演','1');
/*!40000 ALTER TABLE `big_sample` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `experiment_design`
--

DROP TABLE IF EXISTS `experiment_design`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `experiment_design` (
  `id` varchar(11) COLLATE utf8_bin NOT NULL,
  `experiment_design_name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `ctime` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `experiment_design`
--

LOCK TABLES `experiment_design` WRITE;
/*!40000 ALTER TABLE `experiment_design` DISABLE KEYS */;
INSERT INTO `experiment_design` VALUES ('1','仿真方案1','2026-09-06'),('2','仿真方案2','2026-09-07');
/*!40000 ALTER TABLE `experiment_design` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'big_sample_db'
--

--
-- Dumping routines for database 'big_sample_db'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-13 15:57:48
