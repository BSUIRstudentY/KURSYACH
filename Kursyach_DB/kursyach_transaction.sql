-- MySQL dump 10.13  Distrib 8.0.38, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: kursyach
-- ------------------------------------------------------
-- Server version	8.0.39

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `transaction`
--

DROP TABLE IF EXISTS `transaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transaction` (
  `transaction_id` int NOT NULL AUTO_INCREMENT,
  `amount` decimal(10,2) NOT NULL,
  `currency` varchar(3) NOT NULL,
  `quantity` double DEFAULT NULL,
  `to_currency` varchar(4) DEFAULT NULL,
  `transaction_date` datetime(6) NOT NULL,
  `transaction_type` enum('ОБМЕН','ПОКУПКА','ПОПОЛНЕНИЕ','ПРОДАЖА','СНЯТИЕ') NOT NULL,
  `asset_id` int DEFAULT NULL,
  `from_account_id` int DEFAULT NULL,
  `to_account_id` int DEFAULT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`transaction_id`),
  KEY `FKign9bv52nu1v48bk5bews03im` (`asset_id`),
  KEY `FKrff4jlxetafju1e5cks5mfcnk` (`from_account_id`),
  KEY `FKluqt8k2pa8d4gmggx4rhl5vgv` (`to_account_id`),
  KEY `FKsg7jp0aj6qipr50856wf6vbw1` (`user_id`),
  CONSTRAINT `FKign9bv52nu1v48bk5bews03im` FOREIGN KEY (`asset_id`) REFERENCES `asset` (`asset_id`),
  CONSTRAINT `FKluqt8k2pa8d4gmggx4rhl5vgv` FOREIGN KEY (`to_account_id`) REFERENCES `account` (`account_id`),
  CONSTRAINT `FKrff4jlxetafju1e5cks5mfcnk` FOREIGN KEY (`from_account_id`) REFERENCES `account` (`account_id`),
  CONSTRAINT `FKsg7jp0aj6qipr50856wf6vbw1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transaction`
--

LOCK TABLES `transaction` WRITE;
/*!40000 ALTER TABLE `transaction` DISABLE KEYS */;
INSERT INTO `transaction` VALUES (1,175.50,'USD',1,NULL,'2025-05-18 01:50:31.288098','ПОКУПКА',1001,4,NULL,1),(2,500.00,'USD',NULL,'RUB','2025-05-18 02:23:08.976816','ОБМЕН',NULL,4,6,1),(3,55.90,'USD',1,NULL,'2025-05-18 02:24:48.881533','ПОКУПКА',1202,4,NULL,1),(4,5000.00,'USD',NULL,NULL,'2025-05-18 02:31:06.046571','ПОПОЛНЕНИЕ',NULL,NULL,NULL,1),(5,5000.00,'USD',NULL,NULL,'2025-05-18 02:31:11.796480','ПОПОЛНЕНИЕ',NULL,NULL,NULL,1),(6,5000.00,'USD',NULL,NULL,'2025-05-18 14:01:19.583063','ПОПОЛНЕНИЕ',NULL,NULL,3,1),(7,4000.00,'USD',NULL,NULL,'2025-05-18 14:01:39.438684','СНЯТИЕ',NULL,3,NULL,1),(9,5000.00,'USD',NULL,NULL,'2025-05-19 12:53:55.321250','ПОПОЛНЕНИЕ',NULL,NULL,3,1),(10,4000.00,'USD',NULL,'RUB','2025-05-19 12:54:15.586093','ОБМЕН',NULL,3,4,1),(11,123.21,'RUB',1,NULL,'2025-05-21 03:58:40.730666','ПОКУПКА',1045,3,NULL,1),(12,123.21,'RUB',1,NULL,'2025-05-21 04:02:18.783703','ПОКУПКА',1045,3,NULL,1),(13,123.21,'RUB',1,NULL,'2025-05-21 04:04:23.009847','ПОКУПКА',1045,3,NULL,1),(14,120.30,'RUB',1,NULL,'2025-05-21 04:59:48.773880','ПРОДАЖА',1045,3,NULL,1),(15,55.90,'USD',1,NULL,'2025-05-21 05:00:21.525871','ПРОДАЖА',1202,3,NULL,1),(16,175.50,'USD',1,NULL,'2025-05-21 05:05:52.383060','ПРОДАЖА',1001,NULL,3,1),(17,12.00,'USD',NULL,'USD','2025-05-21 12:48:06.693930','ОБМЕН',NULL,3,4,1),(18,177.00,'USD',NULL,'USD','2025-05-21 12:48:42.729586','ОБМЕН',NULL,3,5,1),(19,3213213.00,'USD',NULL,NULL,'2025-05-21 15:25:40.802531','ПОПОЛНЕНИЕ',NULL,NULL,3,1),(20,5002.00,'USD',NULL,'EURO','2025-05-23 00:54:21.025119','ОБМЕН',NULL,3,3,1),(21,5002.00,'USD',NULL,'EURO','2025-05-23 00:54:38.279341','ОБМЕН',NULL,3,6,1);
/*!40000 ALTER TABLE `transaction` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-05-23  2:44:32
