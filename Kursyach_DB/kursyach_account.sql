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
  `user_id` int DEFAULT NULL,
  `name` varchar(50) NOT NULL,
  `account_type` enum('ETF','АКЦИИ','ВАЛЮТНЫЙ','ОБЛИГАЦИИ','ТРЕЙДИНГ','ФЬЮЧЕРСЫ') NOT NULL,
  PRIMARY KEY (`account_id`),
  KEY `FK7m8ru44m93ukyb61dfxw0apf6` (`user_id`),
  CONSTRAINT `FK7m8ru44m93ukyb61dfxw0apf6` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
INSERT INTO `account` VALUES (3,'ACC-1747500697576','{\"BYN\": 20000.0, \"RUB\": 8557.168000000001, \"USD\": 3210205.4775, \"EURO\": 8075.0}','2025-05-17 19:51:37.620082',NULL,'ФЬЮЧИ','ФЬЮЧЕРСЫ'),(4,'ACC-1747501440648','{\"BYN\": 1625.0, \"RUB\": 362120.3, \"USD\": 667.6, \"EURO\": 8.55}','2025-05-17 20:04:00.686495',NULL,'Акционный счёт','АКЦИИ'),(5,'ACC-1747506733643','{\"BYN\": 0.0, \"RUB\": 0.0, \"USD\": 175.0, \"EURO\": 4750.0}','2025-05-17 21:32:13.679120',NULL,'ETF','ETF'),(6,'ACC-1747506752909','{\"BYN\": 0.0, \"EUR\": 0.0, \"RUB\": 45250.0, \"USD\": 0.0, \"EURO\": 4750.0}','2025-05-17 21:32:32.919912',NULL,'no','ВАЛЮТНЫЙ'),(8,'781820b5-eb0f-4bd0-b7ed-fc0f61a84f3e','{\"BYN\": 0.0, \"RUB\": 0.0, \"USD\": 0.0, \"EURO\": 0.0}','2025-05-19 19:24:10.071685',4,'Default Account','ВАЛЮТНЫЙ'),(9,'ACC-1747951316689','{\"BYN\": 0.0, \"RUB\": 0.0, \"USD\": 0.0, \"EURO\": 0.0}','2025-05-23 01:01:56.718947',NULL,'БАЗОВЫЙ','ВАЛЮТНЫЙ'),(10,'ACC-1747953856871','{\"BYN\": 0.0, \"RUB\": 0.0, \"USD\": 0.0, \"EURO\": 0.0}','2025-05-23 01:44:16.904494',NULL,'GOU','ВАЛЮТНЫЙ'),(11,'ACC-1747991568293','{\"BYN\": 0.0, \"RUB\": 0.0, \"USD\": 0.0, \"EURO\": 0.0}','2025-05-23 12:12:48.320301',NULL,'iugyuiyig','ВАЛЮТНЫЙ'),(12,'ACC-1747992980021','{\"BYN\": 0.0, \"RUB\": 0.0, \"USD\": 0.0, \"EURO\": 0.0}','2025-05-23 12:36:20.050712',NULL,'321321','ФЬЮЧЕРСЫ'),(13,'ACC-1747993277184','{\"BYN\": 0.0, \"RUB\": 0.0, \"USD\": 0.0, \"EURO\": 0.0}','2025-05-23 12:41:17.213220',NULL,'111','ВАЛЮТНЫЙ'),(14,'ACC-1747993789474','{\"BYN\": 0.0, \"RUB\": 0.0, \"USD\": 0.0, \"EURO\": 0.0}','2025-05-23 12:49:49.503190',NULL,'222','ВАЛЮТНЫЙ'),(15,'ACC-1747994074755','{\"BYN\": 0.0, \"RUB\": 0.0, \"USD\": 0.0, \"EURO\": 0.0}','2025-05-23 12:54:34.782832',NULL,'0','ВАЛЮТНЫЙ'),(16,'ACC-1747994306990','{\"BYN\": 0.0, \"RUB\": 0.0, \"USD\": 0.0, \"EURO\": 0.0}','2025-05-23 12:58:27.021123',NULL,'БАЗОВЫЙ','ВАЛЮТНЫЙ'),(17,'ACC-1748031562976','{\"BYN\": 0.0, \"EUR\": 49284.58, \"RUB\": 0.0, \"USD\": 0.0}','2025-05-23 23:19:23.007961',NULL,'БАЗОВЫЙ','ВАЛЮТНЫЙ'),(18,'ACC-1748031570355','{\"BYN\": 0.0, \"EUR\": 0.0, \"RUB\": 0.0, \"USD\": 0.0}','2025-05-23 23:19:30.364339',NULL,'ФЬЮЧИ','ФЬЮЧЕРСЫ'),(19,'ACC-1748031579905','{\"BYN\": 0.0, \"EUR\": 8888.0, \"RUB\": 1894443.0, \"USD\": 0.0}','2025-05-23 23:19:39.913276',1,'СЧЁТ ПОЛЬЗОВАТЕЛЯ','ETF'),(20,'ACC-1748031716885','{\"BYN\": 0.0, \"EUR\": 27676.8275, \"RUB\": 0.0, \"USD\": 0.0}','2025-05-23 23:21:56.893472',1,'Банковский','ОБЛИГАЦИИ');
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

-- Dump completed on 2025-05-23 23:28:20
