-- MySQL dump 10.13  Distrib 9.7.0, for Win64 (x86_64)
--
-- Host: localhost    Database: shipping_management
-- ------------------------------------------------------
-- Server version	9.7.0

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
-- Table structure for table `crew_member`
--

DROP TABLE IF EXISTS `crew_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `crew_member` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑸瑰憳ID',
  `crew_no` varchar(30) NOT NULL COMMENT '鑸瑰憳缂栧彿',
  `name` varchar(30) NOT NULL COMMENT '濮撳悕',
  `gender` varchar(10) DEFAULT NULL COMMENT '鎬у埆',
  `phone` varchar(30) DEFAULT NULL COMMENT '鑱旂郴鐢佃瘽',
  `certificate_no` varchar(50) NOT NULL COMMENT '璇佷欢缂栧彿',
  `position` varchar(50) NOT NULL COMMENT '宀椾綅',
  `ship_id` bigint DEFAULT NULL COMMENT '鎵?睘鑸硅埗ID',
  `status` varchar(20) NOT NULL DEFAULT 'UNASSIGNED' COMMENT '鑸瑰憳鐘舵?锛歄N_DUTY鍦ㄥ矖锛孫N_LEAVE浼戝亣锛孶NASSIGNED寰呭垎閰嶏紝RESIGNED绂昏亴',
  `remark` varchar(255) DEFAULT NULL COMMENT '澶囨敞',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_crew_member_no` (`crew_no`),
  UNIQUE KEY `uk_crew_member_certificate_no` (`certificate_no`),
  KEY `idx_crew_member_name` (`name`),
  KEY `idx_crew_member_ship_id` (`ship_id`),
  KEY `idx_crew_member_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鑸瑰憳琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `crew_member`
--

LOCK TABLES `crew_member` WRITE;
/*!40000 ALTER TABLE `crew_member` DISABLE KEYS */;
INSERT INTO `crew_member` VALUES (1,'CREW-001','张三','男','13800000002','CERT-001','船长',1,'ON_DUTY','经验丰富','2026-04-29 15:26:50','2026-04-29 15:43:13'),(2,'CREW-002','李四','男','13800000003','CERT-002','大副',1,'ON_DUTY','负责航行协助','2026-04-29 15:26:50','2026-04-29 15:43:13');
/*!40000 ALTER TABLE `crew_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dictionary_item`
--

DROP TABLE IF EXISTS `dictionary_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dictionary_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '瀛楀吀椤笽D',
  `dict_type` varchar(50) NOT NULL COMMENT '瀛楀吀绫诲瀷锛屽? SHIP_TYPE銆丆ARGO_TYPE銆丳ORT銆丆REW_POSITION',
  `label` varchar(50) NOT NULL COMMENT '鏄剧ず鍚嶇О',
  `value` varchar(50) NOT NULL COMMENT '瀛楀吀鍊',
  `sort` int NOT NULL DEFAULT '0' COMMENT '鎺掑簭鍙',
  `enabled` tinyint NOT NULL DEFAULT '1' COMMENT '鏄?惁鍚?敤锛?鍚︼紝1鏄',
  `remark` varchar(255) DEFAULT NULL COMMENT '澶囨敞',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dictionary_item_type_value` (`dict_type`,`value`),
  KEY `idx_dictionary_item_type` (`dict_type`),
  KEY `idx_dictionary_item_enabled` (`enabled`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='瀛楀吀椤硅〃';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dictionary_item`
--

LOCK TABLES `dictionary_item` WRITE;
/*!40000 ALTER TABLE `dictionary_item` DISABLE KEYS */;
INSERT INTO `dictionary_item` VALUES (1,'SHIP_TYPE','集装箱船','CONTAINER_SHIP',1,1,NULL,'2026-04-29 15:26:50','2026-04-29 15:43:13'),(2,'SHIP_TYPE','散货船','BULK_CARRIER',2,1,NULL,'2026-04-29 15:26:50','2026-04-29 15:43:13'),(3,'SHIP_TYPE','油轮','OIL_TANKER',3,1,NULL,'2026-04-29 15:26:50','2026-04-29 15:43:13'),(4,'CARGO_TYPE','集装箱货物','CONTAINER_CARGO',1,1,NULL,'2026-04-29 15:26:50','2026-04-29 15:43:13'),(5,'CARGO_TYPE','散装货物','BULK_CARGO',2,1,NULL,'2026-04-29 15:26:50','2026-04-29 15:43:13'),(6,'CARGO_TYPE','液体货物','LIQUID_CARGO',3,1,NULL,'2026-04-29 15:26:50','2026-04-29 15:43:13'),(7,'PORT','上海港','SHANGHAI_PORT',1,1,NULL,'2026-04-29 15:26:50','2026-04-29 15:43:13'),(8,'PORT','深圳港','SHENZHEN_PORT',2,1,NULL,'2026-04-29 15:26:50','2026-04-29 15:43:13'),(9,'PORT','宁波舟山港','NINGBO_ZHOUSHAN_PORT',3,1,NULL,'2026-04-29 15:26:50','2026-04-29 15:43:13'),(10,'CREW_POSITION','船长','CAPTAIN',1,1,NULL,'2026-04-29 15:26:50','2026-04-29 15:43:13'),(11,'CREW_POSITION','大副','CHIEF_OFFICER',2,1,NULL,'2026-04-29 15:26:50','2026-04-29 15:43:13'),(12,'CREW_POSITION','轮机长','CHIEF_ENGINEER',3,1,NULL,'2026-04-29 15:26:50','2026-04-29 15:43:13');
/*!40000 ALTER TABLE `dictionary_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `settlement`
--

DROP TABLE IF EXISTS `settlement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `settlement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '缁撶畻ID',
  `settlement_no` varchar(40) NOT NULL COMMENT '缁撶畻缂栧彿',
  `transport_order_id` bigint NOT NULL COMMENT '杩愯緭浠诲姟ID',
  `customer_name` varchar(100) NOT NULL COMMENT '瀹㈡埛鍚嶇О',
  `freight_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '杩愯垂閲戦?',
  `additional_fee` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '闄勫姞璐圭敤',
  `discount_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '浼樻儬閲戦?',
  `receivable_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '搴旀敹閲戦?',
  `received_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '瀹炴敹閲戦?',
  `status` varchar(20) NOT NULL DEFAULT 'UNSETTLED' COMMENT '缁撶畻鐘舵?锛歎NSETTLED鏈?粨绠楋紝PARTIAL閮ㄥ垎缁撶畻锛孲ETTLED宸茬粨绠',
  `settled_at` datetime DEFAULT NULL COMMENT '缁撶畻瀹屾垚鏃堕棿',
  `remark` varchar(255) DEFAULT NULL COMMENT '澶囨敞',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_settlement_no` (`settlement_no`),
  UNIQUE KEY `uk_settlement_transport_order_id` (`transport_order_id`),
  KEY `idx_settlement_customer_name` (`customer_name`),
  KEY `idx_settlement_status` (`status`),
  KEY `idx_settlement_created_at` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='璐㈠姟缁撶畻琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `settlement`
--

LOCK TABLES `settlement` WRITE;
/*!40000 ALTER TABLE `settlement` DISABLE KEYS */;
INSERT INTO `settlement` VALUES (1,'SETTLE-20260429-001',1,'上海某贸易有限公司',20000.00,1000.00,500.00,20500.00,0.00,'UNSETTLED',NULL,'接口验证结算','2026-04-29 16:36:48','2026-04-29 16:36:48');
/*!40000 ALTER TABLE `settlement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ship`
--

DROP TABLE IF EXISTS `ship`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ship` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑸硅埗ID',
  `ship_no` varchar(30) NOT NULL COMMENT '鑸硅埗缂栧彿',
  `ship_name` varchar(50) NOT NULL COMMENT '鑸瑰悕',
  `ship_type` varchar(50) NOT NULL COMMENT '鑸硅埗绫诲瀷',
  `load_capacity` decimal(12,2) NOT NULL COMMENT '杞介噸閲忥紝鍗曚綅锛氬惃',
  `home_port` varchar(50) DEFAULT NULL COMMENT '鎵?睘娓?彛',
  `status` varchar(20) NOT NULL DEFAULT 'IDLE' COMMENT '鑸硅埗鐘舵?锛欼DLE绌洪棽锛孲AILING杩愯緭涓?紝MAINTENANCE缁翠慨涓?紝DISABLED鍋滅敤',
  `remark` varchar(255) DEFAULT NULL COMMENT '澶囨敞',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ship_no` (`ship_no`),
  KEY `idx_ship_name` (`ship_name`),
  KEY `idx_ship_type` (`ship_type`),
  KEY `idx_ship_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鑸硅埗琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ship`
--

LOCK TABLES `ship` WRITE;
/*!40000 ALTER TABLE `ship` DISABLE KEYS */;
INSERT INTO `ship` VALUES (1,'SHIP-001','远航一号','集装箱船',50000.00,'上海港','IDLE','主力运输船舶','2026-04-29 15:26:50','2026-04-29 15:43:13'),(2,'SHIP-002','海运二号','散货船',30000.00,'深圳港','IDLE','常规散货运输','2026-04-29 15:26:50','2026-04-29 15:43:13');
/*!40000 ALTER TABLE `ship` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role`
--

DROP TABLE IF EXISTS `sys_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '瑙掕壊ID',
  `role_name` varchar(30) NOT NULL COMMENT '瑙掕壊鍚嶇О',
  `role_code` varchar(30) NOT NULL COMMENT '瑙掕壊缂栫爜锛屽? ADMIN銆丅USINESS銆乂IEWER',
  `description` varchar(255) DEFAULT NULL COMMENT '瑙掕壊璇存槑',
  `built_in` tinyint NOT NULL DEFAULT '0' COMMENT '鏄?惁鍐呯疆瑙掕壊锛?鍚︼紝1鏄',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_role_code` (`role_code`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='瑙掕壊琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role`
--

LOCK TABLES `sys_role` WRITE;
/*!40000 ALTER TABLE `sys_role` DISABLE KEYS */;
INSERT INTO `sys_role` VALUES (1,'管理员','ADMIN','拥有系统主要管理权限',1,'2026-04-29 15:26:50','2026-04-29 15:43:13'),(2,'业务用户','BUSINESS','可查看船舶、船员、运输和结算等业务数据',1,'2026-04-29 15:26:50','2026-04-30 17:35:40'),(3,'查看用户','VIEWER','仅允许查看业务数据',1,'2026-04-29 15:26:50','2026-04-29 15:43:13');
/*!40000 ALTER TABLE `sys_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鐢ㄦ埛ID',
  `username` varchar(30) NOT NULL COMMENT '鐢ㄦ埛鍚',
  `password` varchar(30) NOT NULL COMMENT '鏄庢枃瀵嗙爜锛岃?绋嬭?璁＄畝鍖栦娇鐢?紝涓嶈繘琛屽姞瀵嗘垨鍝堝笇澶勭悊',
  `real_name` varchar(30) NOT NULL COMMENT '鐪熷疄濮撳悕',
  `phone` varchar(30) DEFAULT NULL COMMENT '鑱旂郴鐢佃瘽',
  `email` varchar(100) DEFAULT NULL COMMENT '閭??',
  `role_id` bigint NOT NULL COMMENT '瑙掕壊ID',
  `status` varchar(20) NOT NULL DEFAULT 'ENABLED' COMMENT '鐢ㄦ埛鐘舵?锛欵NABLED鍚?敤锛孌ISABLED绂佺敤',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_username` (`username`),
  KEY `idx_sys_user_role_id` (`role_id`),
  KEY `idx_sys_user_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鐢ㄦ埛琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,'admin','123456','系统管理员','13800000000','admin@example.com',1,'ENABLED','2026-04-29 15:26:50','2026-04-29 15:43:13'),(2,'business01','123456','业务员一号','13800000001','business01@example.com',2,'ENABLED','2026-04-29 15:26:50','2026-04-30 09:14:15');
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transport_order`
--

DROP TABLE IF EXISTS `transport_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transport_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '杩愯緭浠诲姟ID',
  `order_no` varchar(40) NOT NULL COMMENT '杩愯緭浠诲姟缂栧彿',
  `cargo_name` varchar(100) NOT NULL COMMENT '璐х墿鍚嶇О',
  `cargo_type` varchar(50) NOT NULL COMMENT '璐х墿绫诲瀷',
  `cargo_weight` decimal(12,2) NOT NULL COMMENT '璐х墿閲嶉噺锛屽崟浣嶏細鍚',
  `origin_port` varchar(50) NOT NULL COMMENT '璧疯繍娓',
  `destination_port` varchar(50) NOT NULL COMMENT '鐩?殑娓',
  `ship_id` bigint NOT NULL COMMENT '杩愯緭鑸硅埗ID',
  `customer_name` varchar(100) NOT NULL COMMENT '瀹㈡埛鍚嶇О',
  `customer_phone` varchar(30) DEFAULT NULL COMMENT '瀹㈡埛鑱旂郴鐢佃瘽',
  `planned_departure_time` datetime NOT NULL COMMENT '棰勮?鍑哄彂鏃堕棿',
  `planned_arrival_time` datetime NOT NULL COMMENT '棰勮?鍒拌揪鏃堕棿',
  `actual_departure_time` datetime DEFAULT NULL COMMENT '瀹為檯鍑哄彂鏃堕棿',
  `actual_arrival_time` datetime DEFAULT NULL COMMENT '瀹為檯鍒拌揪鏃堕棿',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '杩愯緭鐘舵?锛歅ENDING寰呭嚭鍙戯紝IN_TRANSIT杩愯緭涓?紝ARRIVED宸插埌杈撅紝CANCELLED宸插彇娑',
  `remark` varchar(255) DEFAULT NULL COMMENT '澶囨敞',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transport_order_no` (`order_no`),
  KEY `idx_transport_order_ship_id` (`ship_id`),
  KEY `idx_transport_order_status` (`status`),
  KEY `idx_transport_order_customer_name` (`customer_name`),
  KEY `idx_transport_order_planned_departure_time` (`planned_departure_time`),
  KEY `idx_transport_order_route` (`origin_port`,`destination_port`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='杩愯緭浠诲姟琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transport_order`
--

LOCK TABLES `transport_order` WRITE;
/*!40000 ALTER TABLE `transport_order` DISABLE KEYS */;
INSERT INTO `transport_order` VALUES (1,'TRANS-20260429-001','电子设备','集装箱货物',1200.50,'上海港','深圳港',1,'上海某贸易有限公司','021-88888888','2026-06-01 08:00:00','2026-06-05 18:00:00',NULL,NULL,'PENDING','接口验证数据','2026-04-29 16:19:26','2026-04-29 16:19:26');
/*!40000 ALTER TABLE `transport_order` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-30 17:36:37
