-- MySQL Administrator dump 1.4
--
-- ------------------------------------------------------
-- Server version	5.0.24a-log


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


--
-- Create schema grid
--

CREATE DATABASE IF NOT EXISTS grid;
USE grid;

DROP TABLE IF EXISTS `grid`.`companies`;
CREATE TABLE  `grid`.`companies` (
  `comp_id` tinyint(4) unsigned NOT NULL auto_increment,
  `comp_name` varchar(255) NOT NULL,
  `HQ_loc` varchar(255) NOT NULL,
  `num_employees` int(20) unsigned NOT NULL,
  `yearly revenue` mediumint(20) unsigned NOT NULL,
  PRIMARY KEY  (`comp_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40000 ALTER TABLE `companies` DISABLE KEYS */;
LOCK TABLES `companies` WRITE;
INSERT INTO `grid`.`companies` VALUES  (101,'Pacific Power','Portland',6000,5700000),
 (102,'Southwest Electric','Phoenix',10000,10000000),(103,'PNW Natural Gas','Seattle',15000,15000000),(104,'Mojave Solar','',5000,2000000),(105,'New England Oil and Gas','Boston',8000,12000000),(106,'Northeast Power Utility','Burlington',9000,14000000);
UNLOCK TABLES;
/*!40000 ALTER TABLE `companies` ENABLE KEYS */;

DROP TABLE IF EXISTS `grid`.`employees`;
CREATE TABLE  `grid`.`employees` (
  `essn` int(20) unsigned NOT NULL,
  `ename` varchar(255) NOT NULL,
  `companyid` tinyint(4) unsigned NOT NULL,
  `position` varchar(255) NOT NULL,
  `address` varchar(255) NOT NULL,
  `city` varchar(255) NOT NULL,
  `salary` mediumint(20) unsigned NOT NULL,
  PRIMARY KEY  (`essn`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40000 ALTER TABLE `employees` DISABLE KEYS */;
LOCK TABLES `employees` WRITE;
INSERT INTO `grid`.`employees` VALUES  (541890067,'Roger Miller',101,'Safety Inspector','678 Lupine Dr. Apt. 10','Beaverton',70000),(890768894,'Dennis Sellers',102,'Process Engineer','102 Sunshine Blvd.','Tucson',110000),(541200067,'Jackie Collins',101,'Process Engineer','189 Sunset Highway','Beaverton',120000),(891208894,'Suzanne Harper',102,'Safety Inspector','25 Blackrock Dr. Apt. 2','Phoenix',65000),(542550067,'Ahmed Wilson',101,'Systems Analyst','95 Stark St.','Portland',115000),(890554895,'Fred Thomas',102,'Systems Analyst','700 Sandstone Court','Phoenix',110000),(891238894,'Jacob Wills',103,'Safety Inspector','78 Rain St.','Seattle',80000),(544320067,'Emily Craig',103,'Systems Analyst','103 Tacoma Rd.','Seattle',125000),(890554894,'Curtis Briggs',103,'Process Engineer','1123 Emerald Ln. Apt. 6','Seattle',100000),(321238894,'Willis Palmer',104,'Safety Inspector','82 Sunny Day Circle','Albuquerque',60000),(124320067,'Catherine Haynes',104,'Systems Analyst','120 Carlyle St.','Albuquerque',95000),(854354894,'Buddy Doyle',104,'Process Engineer','290 Mojave Rd. Apt. 8','Albuquerque',105000),(321238135,'Sally Hemsworth',105,'Safety Inspector','24 Cambridge Dr.','Boston',72000),(124320543,'Jeff Brody',105,'Systems Analyst','212 Lexington St.','Hartford',130000),(854354567,'Lyle Preston',105,'Process Engineer','890 Cambridge Way','Boston',115000),(124343243,'Priscilla Dempsey',106,'Systems Analyst','300 Maple St.','Burlington',140000),(854355647,'Gabriel Gabsworth',106,'Process Engineer','96 Connecticut Dr.','Hartford',120000),(324538135,'Pamela Nicks',106,'Safety Inspector','87 Cedar Rd.','Burlington',80000);
UNLOCK TABLES;
/*!40000 ALTER TABLE `employees` ENABLE KEYS */;

DROP TABLE IF EXISTS `grid`.`cities`;
CREATE TABLE  `grid`.`cities` (
  `name` varchar(255)NOT NULL,
  `state` char(3) NOT NULL,
  `population` mediumint(20) unsigned NOT NULL,
  PRIMARY KEY  (`name`,`state`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40000 ALTER TABLE `cities` DISABLE KEYS */;
LOCK TABLES `cities` WRITE;
INSERT INTO `grid`.`cities` VALUES  ('Portland','OR',600000),('Beaverton','OR',90000),('Seattle','WA',650000),('Tucson','AR',530000),('Phoenix','AZ',1450000),('Albuquerque','NM',556000),('Boston','MA',620000),('Hartford','CT',125000),('Manchester','NH',110000),('Burlington','VT',42000);
UNLOCK TABLES;
/*!40000 ALTER TABLE `cities` ENABLE KEYS */;

DROP TABLE IF EXISTS `grid`.`households`;
CREATE TABLE  `grid`.`households` (
  `homeowner_ssn` int(20) unsigned NOT NULL,
  `homeowner_name` varchar(255) NOT NULL,
  `address` varchar(255) NOT NULL,
  `city` varchar(255) NOT NULL,
  PRIMARY KEY  (`homeowner_ssn`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40000 ALTER TABLE `households` DISABLE KEYS */;
LOCK TABLES `households` WRITE;
INSERT INTO `grid`.`households` VALUES  (504981134,'Ann Seidel','57 Stark St.','Portland'),(789549800,'Mack Rice','102 Lakeshore Lp.','Beaverton'),(556774352,'Emily White','987 Mesa Way Apt. 2','Tucson'),(908774567,'Jack Sussman','786 Sandstone Ln.','Phoenix'),(762458876,'Richard Turnquist','556 Washington St.','Seattle'),(845009987,'Jennifer Orin','56 Scott Dr.','Portland'),(876564412,'Mary Oliver','624 Sandstone Court','Phoenix'),(768901234,'Scott Barrett','5609 Ranch Rd.','Tucson'),(873464412,'Jane Goodwin','765 Sunset Highway','Beaverton'),(768920234,'Scott Barrett','920 Overcast Ave.','Seattle'),(873464403,'Brody Rast','290 Mojave Rd. Apt. 4','Albuquerque'),(468920234,'Jenny Lasz','345 Carlyle St.','Albuquerque'),(873394403,'Betty Gable','67 Cambridge St.','Boston'),(488912234,'Geoff Oren','92 Harvard Ave.','Boston'),(874594403,'Larry Salters','360 Maple St.','Burlington'),(488912235,'Mary Olpen','200 Cedar Rd.','Burlington'),(874594414,'Scott Murphy','876 Wind Rock Ln. Apt. 43','Manchester'),(488912246,'Jen Peri','321 Seaside Ave.','Manchester'),(874594425,'Lex Alder','78 Cherry Orchard Circle','Hartford'),(488912257,'Greta Darmer','900 Heart Ln.','Hartford');
UNLOCK TABLES;
/*!40000 ALTER TABLE `households` ENABLE KEYS */;

DROP TABLE IF EXISTS `grid`.`power_stations`;
CREATE TABLE  `grid`.`power_stations` (
  `id_no` tinyint(4) unsigned NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `state` char(3) NOT NULL,
  `fuel_type` varchar(255) NOT NULL,
  `company_id` tinyint(4) unsigned NOT NULL,
  `MWe_capacity` int(20) unsigned NOT NULL,
  PRIMARY KEY  (`id_no`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `power_stations` WRITE;
INSERT INTO `grid`.`power_stations` VALUES  (1,'Cholla Power Plant','AZ','coal',102,414),(2,'Bonneville Dam','OR','hydro',101,1189),(3,'Yuca Power Plant','AZ','gas',102,264),(4,'Biglow Canyon Wind Farm','OR','wind',101,450),(5,'Chehalis Generation Facility','WA','gas',103,520),(6,'Satsop Combustion Turbine','WA','gas',103,650),(7,'Cimarron Solar Facility','NM','solar',104,37),(8,'Mystic Generating Station','MA','gas',105,2000),(9,'Fore River Generating Stations','MA','gas',105,730),(10,'Comerford Hydroelectric','NH','hydro',106,140),(11,'Sheffield Wind Farm','CT','wind',106,40);
UNLOCK TABLES;
/*!40000 ALTER TABLE `power_stations` ENABLE KEYS */;

DROP TABLE IF EXISTS `grid`.`grid_household_connections`;
CREATE TABLE  `grid`.`grid_household_connections` (
  `home_ssn`int(20) unsigned NOT NULL,
  `grid_id` tinyint(4) unsigned NOT NULL, 
  `annual_kWhr_use` int(20) unsigned NOT NULL,
  `cost_per_kWhr` float(5,5) unsigned NOT NULL,
  PRIMARY KEY  (`home_ssn`,grid_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `grid_household_connections` WRITE;
INSERT INTO `grid`.`grid_household_connections` VALUES  (504981134,2,10932,.12),(789549800,2,12000,.15),(556774352,1,17000,.10),(908774567,1,14000,.11),(762458876,2,9000,.18),(845009987,2,8500,.17),(876564412,1,10500,.09),(768901234,1,11000,.10),(873464412,2,8765,.16),(768920234,2,12064,.18),(873464403,1,11005,.13),(468920234,1,10076,.16),(873394403,3,10076,.14),(488912234,3,12006,.10),(874594403,3,6509,.18),(488912235,3,14000,.09),(874594414,3,10075,.13),(488912246,3,9687,.08),(874594425,3,12000,.11),(488912257,3,9870,.12);
UNLOCK TABLES;
/*!40000 ALTER TABLE `grid_household_connections` ENABLE KEYS */;

DROP TABLE IF EXISTS `grid`.`city_power_co_contracts`;
CREATE TABLE  `grid`.`city_power_co_contracts` (
  `city` varchar(255) NOT NULL,
  `companyid` tinyint(4) unsigned NOT NULL,
  `years_duration` tinyint(4) unsigned NOT NULL,
  `yearly_cost` int(20) unsigned NOT NULL,
  PRIMARY KEY  (`city`,`companyid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `city_power_co_contracts` WRITE;
INSERT INTO `grid`.`city_power_co_contracts` VALUES  ('Tucson',102,5,2500000),('Phoenix',102,3,1500000),('Albuquerque',104,1,700000),('Portland',101,10,5000000),('Beaverton',101,15,3500000),('Seattle',103,7,3000000),('Boston',105,15,4500000),('Burlington',106,10,2000000),('Hartford',105,4,3000000),('Manchester',106,5,2500000);
UNLOCK TABLES;
/*!40000 ALTER TABLE `city_power_co_contracts` ENABLE KEYS */;

DROP TABLE IF EXISTS `grid`.`grids`;
CREATE TABLE  `grid`.`grids` (
  `grid_num` tinyint(4) unsigned NOT NULL auto_increment,
  `area` varchar(255) NOT NULL,
  `num_users` int(20) unsigned NOT NULL,
  `power_id_no` tinyint(4) unsigned NOT NULL,
  KEY  (`grid_num`),
  KEY `FK_grid_1` (`power_id_no`),
  CONSTRAINT `FK_grid_1` FOREIGN KEY (`power_id_no`) REFERENCES `power_stations` (`id_no`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `grids` WRITE;
INSERT INTO `grid`.`grids` VALUES  (1,'Southwest',300000,1),(2,'Pacific Northwest',500000,2),(1,'Southwest',150000,3),(2,'Pacific Northwest',300000,4),(2,'Pacific Northwest',375000,5),(2,'Pacific Northwest',450000,6),(1,'Southwest',12000,7),(3,'Northeast',650000,8),(3,'Northeast',400000,9),(3,'Northeast',55000,10),(3,'Northeast',11000,11);
UNLOCK TABLES;
/*!40000 ALTER TABLE `grids` ENABLE KEYS */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
