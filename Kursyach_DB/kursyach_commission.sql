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
-- Table structure for table `commission`
--

DROP TABLE IF EXISTS `commission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `commission` (
  `commission_id` int NOT NULL AUTO_INCREMENT,
  `amount` decimal(10,2) NOT NULL,
  `commission_date` datetime(6) NOT NULL,
  `currency` varchar(3) NOT NULL,
  `commission_type` enum('ETF','АКЦИИ','ОБЛИГАЦИИ','ОБМЕН','ПОКУПКА','ПОПОЛНЕНИЕ','ПРОДАЖА','СНЯТИЕ','ТРЕЙДИНГ','ФЬЮЧЕРСЫ') NOT NULL,
  `is_active` bit(1) NOT NULL,
  `percentage` decimal(5,2) DEFAULT NULL,
  PRIMARY KEY (`commission_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `commission`
--

LOCK TABLES `commission` WRITE;
/*!40000 ALTER TABLE `commission` DISABLE KEYS */;
INSERT INTO `commission` VALUES (1,0.50,'2025-05-21 03:24:05.379588','USD','АКЦИИ',_binary '',2.00),(2,0.00,'2025-05-21 03:24:05.381598','USD','ОБЛИГАЦИИ',_binary '',0.00),(3,0.00,'2025-05-21 03:24:05.383683','USD','ETF',_binary '',0.00),(4,0.00,'2025-05-21 03:24:05.384773','USD','ТРЕЙДИНГ',_binary '',0.00),(5,0.00,'2025-05-21 03:24:05.387231','USD','ФЬЮЧЕРСЫ',_binary '',0.00),(6,0.00,'2025-05-21 03:24:05.389241','USD','ПОКУПКА',_binary '',0.00),(7,0.00,'2025-05-21 03:24:05.390242','USD','ПРОДАЖА',_binary '',0.00),(8,0.00,'2025-05-21 03:24:05.392600','USD','ПОПОЛНЕНИЕ',_binary '',0.00),(9,0.00,'2025-05-21 03:24:05.394611','USD','СНЯТИЕ',_binary '',0.00),(10,2.00,'2025-05-21 03:24:05.397099','USD','ОБМЕН',_binary '',0.00);
/*!40000 ALTER TABLE `commission` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-05-22 19:35:27
