-- MySQL dump 10.13  Distrib 8.4.6, for Win64 (x86_64)
--
-- Host: localhost    Database: krypton
-- ------------------------------------------------------
-- Server version	8.4.6

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
-- Current Database: `krypton`
--

/*!40000 DROP DATABASE IF EXISTS `krypton`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `krypton` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `krypton`;

--
-- Table structure for table `cart`
--

DROP TABLE IF EXISTS `cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK9emlp6m95v5er2bcqkjsw48he` (`user_id`),
  CONSTRAINT `FKg5uhi8vpsuy0lgloxk2h4w5o6` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart`
--

LOCK TABLES `cart` WRITE;
/*!40000 ALTER TABLE `cart` DISABLE KEYS */;
INSERT INTO `cart` (`id`, `created_at`, `updated_at`, `user_id`) VALUES (1,'2026-08-20 17:08:38.975564','2026-08-20 17:09:00.525852',4),(2,'2026-08-20 17:09:19.281570','2026-08-20 17:09:19.419119',6),(3,'2026-08-20 17:12:43.714777','2026-08-20 17:12:44.753184',7),(5,'2026-08-20 17:29:53.130231','2026-08-20 18:10:32.634171',8),(6,'2026-08-20 17:40:04.298738','2026-08-20 17:40:04.304536',9),(7,'2026-08-20 17:42:43.786630','2026-08-20 17:49:27.029159',2);
/*!40000 ALTER TABLE `cart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart_item`
--

DROP TABLE IF EXISTS `cart_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `quantity` int NOT NULL,
  `cart_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cart_item_cart_product` (`cart_id`,`product_id`),
  KEY `FKqkqmvkmbtiaqn2nfqf25ymfs2` (`product_id`),
  CONSTRAINT `FK1uobyhgl1wvgt1jpccia8xxs3` FOREIGN KEY (`cart_id`) REFERENCES `cart` (`id`),
  CONSTRAINT `FKqkqmvkmbtiaqn2nfqf25ymfs2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_item`
--

LOCK TABLES `cart_item` WRITE;
/*!40000 ALTER TABLE `cart_item` DISABLE KEYS */;
INSERT INTO `cart_item` (`id`, `quantity`, `cart_id`, `product_id`) VALUES (2,5,2,2),(3,12,3,3),(8,1,6,2),(11,1,7,4);
/*!40000 ALTER TABLE `cart_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKt8o6pivur7nn124jehx7cygw5` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` (`id`, `description`, `name`) VALUES (1,'otra','Laptops');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_item`
--

DROP TABLE IF EXISTS `order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_name` varchar(255) NOT NULL,
  `quantity` int NOT NULL,
  `subtotal` decimal(12,2) NOT NULL,
  `unit_price` decimal(12,2) NOT NULL,
  `order_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKt4dc2r9nbvbujrljv3e23iibt` (`order_id`),
  KEY `FKc5uhmwioq5kscilyuchp4w49o` (`product_id`),
  CONSTRAINT `FKc5uhmwioq5kscilyuchp4w49o` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FKt4dc2r9nbvbujrljv3e23iibt` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_item`
--

LOCK TABLES `order_item` WRITE;
/*!40000 ALTER TABLE `order_item` DISABLE KEYS */;
INSERT INTO `order_item` (`id`, `product_name`, `quantity`, `subtotal`, `unit_price`, `order_id`, `product_id`) VALUES (1,'Teclado Krypton',2,259.80,129.90,1,4),(2,'Teclado Krypton',3,389.70,129.90,2,4),(3,'Teclado Krypton',3,389.70,129.90,3,4),(4,'Teclado Krypton',1,129.90,129.90,4,4),(5,'Producto Stress',1,10.00,10.00,5,3),(6,'Mouse Krypton Pro',2,259.80,129.90,6,2);
/*!40000 ALTER TABLE `order_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `customer_doc` varchar(11) NOT NULL,
  `customer_name` varchar(150) NOT NULL,
  `discount` decimal(12,2) NOT NULL,
  `document_type` enum('BOLETA','FACTURA') NOT NULL,
  `igv` decimal(12,2) NOT NULL,
  `order_date` datetime(6) NOT NULL,
  `paid_at` datetime(6) DEFAULT NULL,
  `payment_method` enum('CREDIT_CARD','DEBIT_CARD','YAPE') DEFAULT NULL,
  `shipping_cost` decimal(12,2) NOT NULL,
  `status` enum('CANCELADA','CONFIRMADA','ENTREGADO','ENVIADO','PENDIENTE') NOT NULL,
  `subtotal` decimal(12,2) NOT NULL,
  `total` decimal(12,2) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_orders_user_date` (`user_id`,`order_date`),
  KEY `idx_orders_status` (`status`),
  CONSTRAINT `FK32ql8ubntj5uh44ph9659tiih` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` (`id`, `customer_doc`, `customer_name`, `discount`, `document_type`, `igv`, `order_date`, `paid_at`, `payment_method`, `shipping_cost`, `status`, `subtotal`, `total`, `user_id`) VALUES (1,'12345678','Juan Perez',0.00,'BOLETA',42.68,'2026-08-20 17:29:53.480207','2026-08-20 17:30:13.679592','YAPE',20.00,'ENTREGADO',259.80,279.80,8),(2,'20123456789','Krypton SAC',0.00,'FACTURA',59.45,'2026-08-20 17:30:50.719025',NULL,NULL,0.00,'CANCELADA',389.70,389.70,8),(3,'12345678','Juan Perez',0.00,'BOLETA',59.45,'2026-08-20 17:30:51.404285',NULL,NULL,0.00,'PENDIENTE',389.70,389.70,8),(4,'70123456','Jason Davila',0.00,'BOLETA',22.87,'2026-08-20 17:36:33.573438','2026-08-20 17:37:00.192200','YAPE',20.00,'CONFIRMADA',129.90,149.90,8),(5,'12456655','Gabriel',0.00,'BOLETA',4.58,'2026-08-20 17:43:13.022759','2026-08-20 17:43:31.369924','YAPE',20.00,'ENVIADO',10.00,30.00,2),(6,'11111111','Regresion Test',0.00,'BOLETA',42.68,'2026-08-20 18:10:32.747582',NULL,NULL,20.00,'CANCELADA',259.80,279.80,8);
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_image`
--

DROP TABLE IF EXISTS `product_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_image` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `display_order` smallint NOT NULL,
  `is_cover` bit(1) NOT NULL,
  `path` varchar(500) NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1n91c4vdhw8pa4frngs4qbbvs` (`product_id`),
  CONSTRAINT `FK1n91c4vdhw8pa4frngs4qbbvs` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_image`
--

LOCK TABLES `product_image` WRITE;
/*!40000 ALTER TABLE `product_image` DISABLE KEYS */;
INSERT INTO `product_image` (`id`, `created_at`, `display_order`, `is_cover`, `path`, `product_id`) VALUES (2,'2026-08-20 17:00:49.388887',0,_binary '','71db8872-ea3e-41ee-bd8a-69a69f9a1bd0.png',1),(3,'2026-08-20 17:41:55.139520',0,_binary '','73299bec-576d-49b3-bd2b-914cf855b33e.jpg',3),(4,'2026-08-20 17:42:11.619859',1,_binary '\0','b2e68c62-3785-4a8f-bcfe-1ff0b036ddf3.jpg',3);
/*!40000 ALTER TABLE `product_image` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `price` decimal(12,2) NOT NULL,
  `sku` varchar(255) NOT NULL,
  `stock` int NOT NULL,
  `category_id` bigint NOT NULL,
  `stock_min` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKfhmd06dsmj6k0n90swsh8ie9g` (`sku`),
  KEY `FKog2rp4qthbtt2lfyhfo32lsw9` (`category_id`),
  CONSTRAINT `FKog2rp4qthbtt2lfyhfo32lsw9` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` (`id`, `active`, `description`, `image_url`, `name`, `price`, `sku`, `stock`, `category_id`, `stock_min`) VALUES (1,_binary '','Ultrabook 14 pulgadas','http://localhost:8080/api/uploads/images/71db8872-ea3e-41ee-bd8a-69a69f9a1bd0.png','Krypton Book 14',3299.90,'KR-LAP-001',15,1,0),(2,_binary '','Inalambrico',NULL,'Mouse Krypton Pro',129.90,'KR-MOU-777',20,1,25),(3,_binary '',NULL,NULL,'Producto Stress',10.00,'KR-STRESS-1',499,1,10),(4,_binary '','Ultrabook',NULL,'Teclado Krypton',129.90,'KR-ORD-100',7,1,0);
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stock_movement`
--

DROP TABLE IF EXISTS `stock_movement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_movement` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `quantity` int NOT NULL,
  `reason` varchar(100) NOT NULL,
  `reference` varchar(50) NOT NULL,
  `type` enum('ENTRADA','SALIDA') NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_stock_movement_product` (`product_id`,`created_at`),
  CONSTRAINT `FKhj6nusfm483wy67vt7v7hjdma` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stock_movement`
--

LOCK TABLES `stock_movement` WRITE;
/*!40000 ALTER TABLE `stock_movement` DISABLE KEYS */;
INSERT INTO `stock_movement` (`id`, `created_at`, `quantity`, `reason`, `reference`, `type`, `product_id`) VALUES (1,'2026-08-20 17:29:53.497148',2,'Checkout','ORDEN-1','SALIDA',4),(2,'2026-08-20 17:30:50.719025',3,'Checkout','ORDEN-2','SALIDA',4),(3,'2026-08-20 17:30:50.908049',3,'Cancelación de pedido','ORDEN-2','ENTRADA',4),(4,'2026-08-20 17:30:51.419909',3,'Checkout','ORDEN-3','SALIDA',4),(5,'2026-08-20 17:36:33.609151',1,'Checkout','ORDEN-4','SALIDA',4),(6,'2026-08-20 17:43:13.035949',1,'Checkout','ORDEN-5','SALIDA',3),(7,'2026-08-20 18:09:28.194869',21,'Ajuste de inventario','AJUSTE-PROD-4','ENTRADA',4),(8,'2026-08-20 18:09:28.344446',18,'Ajuste de inventario','AJUSTE-PROD-4','SALIDA',4),(9,'2026-08-20 18:10:32.778833',2,'Checkout','ORDEN-6','SALIDA',2),(10,'2026-08-20 18:10:33.483316',2,'Cancelación de pedido','ORDEN-6','ENTRADA',2),(11,'2026-08-20 18:13:56.288469',37,'Ajuste de inventario','AJUSTE-PROD-2','ENTRADA',2),(12,'2026-08-20 18:15:08.024154',22,'Ajuste de inventario','AJUSTE-PROD-2','SALIDA',2),(13,'2026-08-20 18:25:15.154620',499,'Ajuste de inventario','AJUSTE-PROD-3','SALIDA',3),(14,'2026-08-20 18:27:41.938335',499,'Ajuste de inventario','AJUSTE-PROD-3','ENTRADA',3);
/*!40000 ALTER TABLE `stock_movement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('ADMIN','CLIENTE') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` (`id`, `active`, `created_at`, `email`, `name`, `password`, `role`) VALUES (1,_binary '','2026-08-20 16:39:28.241126','test1787243967@krypton.pe','Test User','$2a$10$8zTxco8pSNbIovwiqKreMe4B0V9u7AgVUdj0DxQqNenl7z98CDqRe','CLIENTE'),(2,_binary '','2026-08-20 16:46:57.583540','admin@krypton.pe','Administrador','$2a$10$EZvKZSvEXq2iLCKBvvDt.uEMgo0IgUnL9SsjaFSQchyV9MSMPhug6','ADMIN'),(3,_binary '','2026-08-20 16:47:43.507047','admin2@krypton.pe','Segundo Admin','$2a$10$CMu.IaY9UOXKo3uAQi.LPeXOXa/hYNEBBs738p.HmSfcgP2aLKuk6','ADMIN'),(4,_binary '','2026-08-20 17:08:38.149662','cliente1@krypton.pe','Cliente 1','$2a$10$6fWRjv6Nx3eO/mWX.Pe5MuIZagwscMdl/ERE3AWQuFj3E4fHMNl5G','CLIENTE'),(5,_binary '','2026-08-20 17:08:38.349994','cliente2@krypton.pe','Cliente 2','$2a$10$kXcA0DNMwefP2Tm/PIeNheGRsyN6H5ZBAvbODYgVwCsMzPPBgHWR6','CLIENTE'),(6,_binary '','2026-08-20 17:09:19.003210','cliente3@krypton.pe','Cliente 3','$2a$10$Fzesp6kgQ716q2ps23rG3OOVyw0Ie/nd7BtLerJeskdVIXrisHQ7C','CLIENTE'),(7,_binary '','2026-08-20 17:12:43.242484','race@krypton.pe','Race','$2a$10$uX0myb7aiNY.ncFeebUbgu/zO2j1VBnQkX5RZuNHhBKElOvOpxKAO','CLIENTE'),(8,_binary '','2026-08-20 17:29:52.733078','compra@krypton.pe','Compra','$2a$10$eE/X5PmR4JTNbx9NdScTtuX59Dz1FkkNfnvbm1uhraORrJ/Yl0Xkm','CLIENTE'),(9,_binary '','2026-08-20 17:39:51.120635','cliente@gmail.com','Cliente','$2a$10$ddKrM.4abW6x5OftQ5f3Cep7UatdMTCMxiCguh9v.WYzO8Tw0HP9W','CLIENTE'),(10,_binary '','2026-08-20 23:38:50.312157','bartolito@gmail.com','Bartolo','$2a$10$aLTzX8TWdMUKeTfOWzKJ1uxFsswpbKdn5DdMRa8swWJZ4Sj4A8r/O','CLIENTE');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed
