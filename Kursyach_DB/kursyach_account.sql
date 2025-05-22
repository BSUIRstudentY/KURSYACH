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
-- Table structure for table `account`
--

DROP TABLE IF EXISTS `account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account` (
  `account_id` int NOT NULL AUTO_INCREMENT,
  `account_number` varchar(50) NOT NULL,
  `balances` json NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `user_id` int NOT NULL,
  `name` varchar(50) NOT NULL,
  `account_type` enum('ETF','АКЦИИ','ВАЛЮТНЫЙ','ОБЛИГАЦИИ','ТРЕЙДИНГ','ФЬЮЧЕРСЫ') NOT NULL,
  PRIMARY KEY (`account_id`),
  KEY `FK7m8ru44m93ukyb61dfxw0apf6` (`user_id`),
  CONSTRAINT `FK7m8ru44m93ukyb61dfxw0apf6` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
INSERT INTO `account` VALUES (3,'ACC-1747500697576','{\"BYN\": 20000.0, \"RUB\": 8680.378, \"USD\": 3225211.4775, \"EURO\": 3325.0}','2025-05-17 19:51:37.620082',1,'ФЬЮЧИ','ФЬЮЧЕРСЫ'),(4,'ACC-1747501440648','{\"BYN\": 1625.0, \"RUB\": 362000.0, \"USD\": 667.6, \"EURO\": 8.55}','2025-05-17 20:04:00.686495',1,'Акционный счёт','АКЦИИ'),(5,'ACC-1747506733643','{\"BYN\": 0.0, \"EUR\": 0.0, \"RUB\": 0.0, \"USD\": 175.0}','2025-05-17 21:32:13.679120',1,'ETF','ETF'),(6,'ACC-1747506752909','{\"BYN\": 0.0, \"EUR\": 0.0, \"RUB\": 45250.0, \"USD\": 0.0}','2025-05-17 21:32:32.919912',1,'no','ВАЛЮТНЫЙ'),(8,'781820b5-eb0f-4bd0-b7ed-fc0f61a84f3e','{\"BYN\": 0.0, \"RUB\": 0.0, \"USD\": 0.0, \"EURO\": 0.0}','2025-05-19 19:24:10.071685',4,'Default Account','ВАЛЮТНЫЙ');
/*!40000 ALTER TABLE `account` ENABLE KEYS */;
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
