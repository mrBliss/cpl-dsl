



-- ---
-- Globals
-- ---

-- SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
-- SET FOREIGN_KEY_CHECKS=0;

-- ---
-- Table 'City'
-- 
-- ---

DROP TABLE IF EXISTS `City`;
		
CREATE TABLE `City` (
  `id` INT NOT NULL AUTO_INCREMENT DEFAULT NULL,
  `name` VARCHAR(100) NULL DEFAULT NULL,
  `id_Country` INT NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
);

-- ---
-- Table 'Airport'
-- 
-- ---

DROP TABLE IF EXISTS `Airport`;
		
CREATE TABLE `Airport` (
  `code` VARCHAR(3) NOT NULL DEFAULT '???',
  `name` VARCHAR(100) NULL DEFAULT NULL,
  `id_City` INT NULL DEFAULT NULL,
  PRIMARY KEY (`code`)
);

-- ---
-- Table 'Country'
-- 
-- ---

DROP TABLE IF EXISTS `Country`;
		
CREATE TABLE `Country` (
  `id` INT NOT NULL AUTO_INCREMENT DEFAULT NULL,
  `name` VARCHAR(100) NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
);

-- ---
-- Table 'Connection'
-- 
-- ---

DROP TABLE IF EXISTS `Connection`;
		
CREATE TABLE `Connection` (
  `distance` DOUBLE NULL DEFAULT NULL,
  `code_fromAirport` VARCHAR(3) NOT NULL DEFAULT '???',
  `code_toAirport` VARCHAR(3) NOT NULL DEFAULT '???',
  UNIQUE KEY (`code_fromAirport`, `code_toAirport`)
);

-- ---
-- Table 'FlightTemplate'
-- 
-- ---

DROP TABLE IF EXISTS `FlightTemplate`;
		
CREATE TABLE `FlightTemplate` (
  `code_fromAirport_Connection` VARCHAR(3) NULL DEFAULT NULL,
  `code_toAirport_Connection` VARCHAR(3) NULL DEFAULT NULL,
  `code` VARCHAR(4) NOT NULL DEFAULT '????',
  `code_AirlineCompany` VARCHAR(3) NOT NULL DEFAULT 'XXX',
  UNIQUE KEY (`code`, `code_AirlineCompany`)
);

-- ---
-- Table 'AirlineCompany'
-- 
-- ---

DROP TABLE IF EXISTS `AirlineCompany`;
		
CREATE TABLE `AirlineCompany` (
  `code` VARCHAR(3) NOT NULL DEFAULT '???',
  `name` VARCHAR(100) NULL DEFAULT NULL,
  PRIMARY KEY (`code`)
);

-- ---
-- Table 'Class'
-- 
-- ---

DROP TABLE IF EXISTS `Class`;
		
CREATE TABLE `Class` (
  `code_AirlineCompany` VARCHAR(3) NOT NULL DEFAULT 'XXX',
  `id` INT NOT NULL AUTO_INCREMENT DEFAULT NULL,
  `name` VARCHAR(100) NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
);

-- ---
-- Table 'Seat'
-- 
-- ---

DROP TABLE IF EXISTS `Seat`;
		
CREATE TABLE `Seat` (
  `id` INT NOT NULL AUTO_INCREMENT DEFAULT NULL,
  `id_Flight` INT NULL DEFAULT NULL,
  `number` INT NULL DEFAULT NULL,
  `id_Class` INT NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY (`id_Flight`, `number`)
);

-- ---
-- Table 'Ticket'
-- 
-- ---

DROP TABLE IF EXISTS `Ticket`;
		
CREATE TABLE `Ticket` (
  `id` INT NOT NULL AUTO_INCREMENT DEFAULT NULL,
  `price` DECIMAL(19,4) NULL DEFAULT NULL,
  `id_Seat` INT NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
);

-- ---
-- Table 'Flight'
-- 
-- ---

DROP TABLE IF EXISTS `Flight`;
		
CREATE TABLE `Flight` (
  `id` INT NOT NULL AUTO_INCREMENT DEFAULT NULL,
  `date` DATETIME NULL DEFAULT NULL,
  `id_FlightMoment` INT NULL DEFAULT NULL,
  `code_Airplane` VARCHAR(30) NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
);

-- ---
-- Table 'FlightMoment'
-- 
-- ---

DROP TABLE IF EXISTS `FlightMoment`;
		
CREATE TABLE `FlightMoment` (
  `code_FlightTemplate` VARCHAR(4) NULL DEFAULT NULL,
  `code_AirlineCompany_FlightTemplate` VARCHAR(3) NOT NULL DEFAULT 'XXX',
  `weekday` ENUM('MON','TUE','WED','THU','FRI','SAT','SUN') NULL DEFAULT NULL,
  `time` TIME NULL DEFAULT NULL,
  `id` INT NOT NULL AUTO_INCREMENT DEFAULT NULL,
  UNIQUE KEY (`weekday`, `time`, `code_FlightTemplate`, `code_AirlineCompany_FlightTemplate`),
  PRIMARY KEY (`id`)
);

-- ---
-- Table 'Airplane'
-- 
-- ---

DROP TABLE IF EXISTS `Airplane`;
		
CREATE TABLE `Airplane` (
  `code` VARCHAR(30) NOT NULL DEFAULT '????',
  `id_AirplaneModel` INT NULL DEFAULT NULL,
  PRIMARY KEY (`code`)
);

-- ---
-- Table 'AirplaneModel'
-- 
-- ---

DROP TABLE IF EXISTS `AirplaneModel`;
		
CREATE TABLE `AirplaneModel` (
  `id` INT NOT NULL AUTO_INCREMENT DEFAULT NULL,
  `name` VARCHAR(50) NULL DEFAULT NULL,
  `maxSpeed` DOUBLE NULL DEFAULT NULL,
  `maxNbOfSeats` MEDIUMINT NULL DEFAULT NULL,
  `id_Manufacturer` INT NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
);

-- ---
-- Table 'Manufacturer'
-- 
-- ---

DROP TABLE IF EXISTS `Manufacturer`;
		
CREATE TABLE `Manufacturer` (
  `name` VARCHAR(100) NOT NULL DEFAULT 'NULL',
  `id` INT NOT NULL AUTO_INCREMENT DEFAULT NULL,
  PRIMARY KEY (`id`)
);

-- ---
-- Foreign Keys 
-- ---

ALTER TABLE `City` ADD FOREIGN KEY (id_Country) REFERENCES `Country` (`id`);
ALTER TABLE `Airport` ADD FOREIGN KEY (id_City) REFERENCES `City` (`id`);
ALTER TABLE `Connection` ADD FOREIGN KEY (code_fromAirport) REFERENCES `Airport` (`code`);
ALTER TABLE `Connection` ADD FOREIGN KEY (code_toAirport) REFERENCES `Airport` (`code`);
ALTER TABLE `FlightTemplate` ADD FOREIGN KEY (code_fromAirport_Connection) REFERENCES `Connection` (`code_fromAirport`);
ALTER TABLE `FlightTemplate` ADD FOREIGN KEY (code_toAirport_Connection) REFERENCES `Connection` (`code_toAirport`);
ALTER TABLE `FlightTemplate` ADD FOREIGN KEY (code_AirlineCompany) REFERENCES `AirlineCompany` (`code`);
ALTER TABLE `Class` ADD FOREIGN KEY (code_AirlineCompany) REFERENCES `AirlineCompany` (`code`);
ALTER TABLE `Seat` ADD FOREIGN KEY (id_Flight) REFERENCES `Flight` (`id`);
ALTER TABLE `Seat` ADD FOREIGN KEY (id_Class) REFERENCES `Class` (`id`);
ALTER TABLE `Ticket` ADD FOREIGN KEY (id_Seat) REFERENCES `Seat` (`id`);
ALTER TABLE `Flight` ADD FOREIGN KEY (id_FlightMoment) REFERENCES `FlightMoment` (`id`);
ALTER TABLE `Flight` ADD FOREIGN KEY (code_Airplane) REFERENCES `Airplane` (`code`);
ALTER TABLE `FlightMoment` ADD FOREIGN KEY (code_FlightTemplate) REFERENCES `FlightTemplate` (`code`);
ALTER TABLE `FlightMoment` ADD FOREIGN KEY (code_AirlineCompany_FlightTemplate) REFERENCES `FlightTemplate` (`code_AirlineCompany`);
ALTER TABLE `Airplane` ADD FOREIGN KEY (id_AirplaneModel) REFERENCES `AirplaneModel` (`id`);
ALTER TABLE `AirplaneModel` ADD FOREIGN KEY (id_Manufacturer) REFERENCES `Manufacturer` (`id`);

-- ---
-- Table Properties
-- ---

-- ALTER TABLE `City` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `Airport` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `Country` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `Connection` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `FlightTemplate` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `AirlineCompany` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `Class` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `Seat` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `Ticket` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `Flight` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `FlightMoment` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `Airplane` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `AirplaneModel` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `Manufacturer` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ---
-- Test Data
-- ---

-- INSERT INTO `City` (`id`,`name`,`id_Country`) VALUES
-- ('','','');
-- INSERT INTO `Airport` (`code`,`name`,`id_City`) VALUES
-- ('','','');
-- INSERT INTO `Country` (`id`,`name`) VALUES
-- ('','');
-- INSERT INTO `Connection` (`distance`,`code_fromAirport`,`code_toAirport`) VALUES
-- ('','','');
-- INSERT INTO `FlightTemplate` (`code_fromAirport_Connection`,`code_toAirport_Connection`,`code`,`code_AirlineCompany`) VALUES
-- ('','','','');
-- INSERT INTO `AirlineCompany` (`code`,`name`) VALUES
-- ('','');
-- INSERT INTO `Class` (`code_AirlineCompany`,`id`,`name`) VALUES
-- ('','','');
-- INSERT INTO `Seat` (`id`,`id_Flight`,`number`,`id_Class`) VALUES
-- ('','','','');
-- INSERT INTO `Ticket` (`id`,`price`,`id_Seat`) VALUES
-- ('','','');
-- INSERT INTO `Flight` (`id`,`date`,`id_FlightMoment`,`code_Airplane`) VALUES
-- ('','','','');
-- INSERT INTO `FlightMoment` (`code_FlightTemplate`,`code_AirlineCompany_FlightTemplate`,`weekday`,`time`,`id`) VALUES
-- ('','','','','');
-- INSERT INTO `Airplane` (`code`,`id_AirplaneModel`) VALUES
-- ('','');
-- INSERT INTO `AirplaneModel` (`id`,`name`,`maxSpeed`,`maxNbOfSeats`,`id_Manufacturer`) VALUES
-- ('','','','','');
-- INSERT INTO `Manufacturer` (`name`,`id`) VALUES
-- ('','');


