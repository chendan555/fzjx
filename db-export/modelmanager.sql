-- MySQL dump 10.13  Distrib 5.7.40, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: modelmanager
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
-- Current Database: `modelmanager`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `modelmanager` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_bin */;

USE `modelmanager`;

--
-- Table structure for table `compdataitem`
--

DROP TABLE IF EXISTS `compdataitem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `compdataitem` (
  `id` int(11) NOT NULL,
  `type` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `compdataitem`
--

LOCK TABLES `compdataitem` WRITE;
/*!40000 ALTER TABLE `compdataitem` DISABLE KEYS */;
INSERT INTO `compdataitem` VALUES (1,'messageData');
/*!40000 ALTER TABLE `compdataitem` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dataitem`
--

DROP TABLE IF EXISTS `dataitem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dataitem` (
  `simulatmutual_id` int(11) DEFAULT NULL,
  `meaning` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `type` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `instruction_type` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `compdata_id` varchar(255) COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dataitem`
--

LOCK TABLES `dataitem` WRITE;
/*!40000 ALTER TABLE `dataitem` DISABLE KEYS */;
INSERT INTO `dataitem` VALUES (1,'弹id','weaponUID','string','simpleType',NULL),(1,'是否命中','isHit','string','simpleType',NULL),(1,'属性3','column3','string','simpleType',NULL),(2,'发射平台UID','platformUID','long','simpleType',NULL),(2,'发射平台类型','platformType','string','simpleType',NULL),(2,'导弹UID','missileUID','long','simpleType',NULL),(2,'导弹类型','missileType','string','simpleType',NULL),(2,'发射时间','time','double','simpleType',NULL),(9,'当前时间','simTime','long','simpleType',NULL),(9,'导弹id','missileUID','long','simpleType',NULL),(9,'经度','longitude','double','simpleType',NULL),(9,'维度','latitude','int','simpleType',NULL),(9,'高度','altitude','long','simpleType',NULL),(9,'北向速度','vx','string','simpleType',NULL),(9,'天向速度','vy','string','simpleType',NULL),(9,'东向速度','vz','long','simpleType',NULL),(9,'滚转角','game','string','simpleType',NULL),(9,'俯仰角','theta','long','simpleType',NULL),(9,'偏航角','psi','long','simpleType',NULL),(9,'当前阶段','phase','long','simpleType',NULL),(9,'合速度','velocity','string','simpleType',NULL),(10,'弹id','entityUID','string','simpleType',NULL),(10,'平台类型','platformType','string','simpleType',NULL),(10,'导弹类型','entityType','string','simpleType',NULL),(11,'类型','type','string','simpleType',NULL),(11,'消息','messageData','messageData','complexType',NULL),(NULL,'平台类型','ptType','string','simpleType','1'),(NULL,'平台id','ptId','string','simpleType','1');
/*!40000 ALTER TABLE `dataitem` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `simulatmutual`
--

DROP TABLE IF EXISTS `simulatmutual`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `simulatmutual` (
  `id` int(11) NOT NULL,
  `name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `simulatmutual`
--

LOCK TABLES `simulatmutual` WRITE;
/*!40000 ALTER TABLE `simulatmutual` DISABLE KEYS */;
INSERT INTO `simulatmutual` VALUES (1,'HIT'),(2,'ATTACK'),(9,'YJ18BTrajectoryData'),(10,'MISSILELAUNCHTIME'),(11,'TEST');
/*!40000 ALTER TABLE `simulatmutual` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `topic`
--

DROP TABLE IF EXISTS `topic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `topic` (
  `id` int(11) NOT NULL,
  `name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `type` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `remark` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `topic`
--

LOCK TABLES `topic` WRITE;
/*!40000 ALTER TABLE `topic` DISABLE KEYS */;
INSERT INTO `topic` VALUES (1,'TOPIC_MSG_HIT','HIT','命中'),(2,'TOPIC_MSG_TOPIC_INTEL_AttackBatReport','ATTACK','进攻弹发射'),(9,'TOPIC_QY_YJ18BTrajectoryData','YJ18BTrajectoryData','弹道数据'),(10,'TOPIC_QY_MISSILELAUNCHTIME','MISSILELAUNCHTIME','发射时刻（弹id + 平台类型）'),(11,'TEST','TEST','测试数据');
/*!40000 ALTER TABLE `topic` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'modelmanager'
--

--
-- Dumping routines for database 'modelmanager'
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
