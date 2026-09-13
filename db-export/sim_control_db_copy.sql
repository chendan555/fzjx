-- MySQL dump 10.13  Distrib 5.7.40, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: sim_control_db_copy
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
-- Current Database: `sim_control_db_copy`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `sim_control_db_copy` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_bin */;

USE `sim_control_db_copy`;

--
-- Table structure for table `task_design_example`
--

DROP TABLE IF EXISTS `task_design_example`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `task_design_example` (
  `id` varchar(11) COLLATE utf8_bin NOT NULL,
  `name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `deduce_config_id` varchar(11) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_design_example`
--

LOCK TABLES `task_design_example` WRITE;
/*!40000 ALTER TABLE `task_design_example` DISABLE KEYS */;
INSERT INTO `task_design_example` VALUES ('1','样本1','1'),('2','样本2','1');
/*!40000 ALTER TABLE `task_design_example` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task_example_correlation`
--

DROP TABLE IF EXISTS `task_example_correlation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `task_example_correlation` (
  `id` varchar(11) COLLATE utf8_bin NOT NULL,
  `deduce_task_id` int(11) DEFAULT NULL,
  `task_design_example_id` varchar(11) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_example_correlation`
--

LOCK TABLES `task_example_correlation` WRITE;
/*!40000 ALTER TABLE `task_example_correlation` DISABLE KEYS */;
INSERT INTO `task_example_correlation` VALUES ('1',123,'1'),('2',456,'1'),('3',789,'2');
/*!40000 ALTER TABLE `task_example_correlation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'sim_control_db_copy'
--

--
-- Dumping routines for database 'sim_control_db_copy'
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
