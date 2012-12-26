
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
  `id_Country` INT NOT NULL DEFAULT NULL,
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
  `id_City` INT NOT NULL DEFAULT NULL,
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
  `id` INT NOT NULL AUTO_INCREMENT DEFAULT NULL,
  `code_fromAirport` VARCHAR(3) NOT NULL DEFAULT '???',
  `code_toAirport` VARCHAR(3) NOT NULL DEFAULT '???',
  `distance` DOUBLE NOT NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY (`code_fromAirport`, `code_toAirport`)
);

-- ---
-- Table 'FlightTemplate'
--
-- ---

DROP TABLE IF EXISTS `FlightTemplate`;

CREATE TABLE `FlightTemplate` (
  `id` INT NOT NULL AUTO_INCREMENT DEFAULT NULL,
  `code` VARCHAR(4) NOT NULL DEFAULT '????',
  `code_AirlineCompany` VARCHAR(3) NOT NULL DEFAULT '???',
  `id_Connection` INT NOT NULL DEFAULT NULL,
  UNIQUE KEY (`code`, `code_AirlineCompany`),
  PRIMARY KEY (`id`)
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
-- Table 'SeatType'
--
-- ---

DROP TABLE IF EXISTS `SeatType`;

CREATE TABLE `SeatType` (
  `id` INT NOT NULL AUTO_INCREMENT DEFAULT NULL,
  `name` VARCHAR(100) NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
);

-- ---
-- Table 'Flight'
--
-- ---

DROP TABLE IF EXISTS `Flight`;

CREATE TABLE `Flight` (
  `id` INT NOT NULL AUTO_INCREMENT DEFAULT NULL,
  `id_FlightTemplate` INT NOT NULL DEFAULT NULL,
  `time` DATETIME NOT NULL DEFAULT 'NULL',
  `code_AirplaneModel` INT NOT NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY (`time`)
);

-- ---
-- Table 'AirplaneModel'
--
-- ---

DROP TABLE IF EXISTS `AirplaneModel`;

CREATE TABLE `AirplaneModel` (
  `id` INT NOT NULL AUTO_INCREMENT DEFAULT NULL,
  `name` VARCHAR(50) NULL DEFAULT NULL,
  `cruiseSpeed` DOUBLE NOT NULL DEFAULT NULL,
  `maxNbOfSeats` MEDIUMINT NOT NULL DEFAULT NULL,
  `id_Manufacturer` INT NOT NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
);

-- ---
-- Table 'Manufacturer'
--
-- ---

DROP TABLE IF EXISTS `Manufacturer`;

CREATE TABLE `Manufacturer` (
  `id` INT NOT NULL AUTO_INCREMENT DEFAULT NULL,
  `name` VARCHAR(100) NOT NULL DEFAULT 'NULL',
  PRIMARY KEY (`id`)
);

-- ---
-- Table 'SeatPricing'
--
-- ---

DROP TABLE IF EXISTS `SeatPricing`;

CREATE TABLE `SeatPricing` (
  `id` INT NOT NULL AUTO_INCREMENT DEFAULT NULL,
  `id_SeatType` INT NOT NULL DEFAULT NULL,
  `id_Flight` INT NOT NULL DEFAULT NULL,
  `price` DECIMAL(19,4) NOT NULL DEFAULT NULL,
  `nbSeats` MEDIUMINT NOT NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY (`id_SeatType`, `id_Flight`)
);

-- ---
-- Foreign Keys
-- ---

ALTER TABLE `City` ADD FOREIGN KEY (id_Country) REFERENCES `Country` (`id`);
ALTER TABLE `Airport` ADD FOREIGN KEY (id_City) REFERENCES `City` (`id`);
ALTER TABLE `Connection` ADD FOREIGN KEY (code_fromAirport) REFERENCES `Airport` (`code`);
ALTER TABLE `Connection` ADD FOREIGN KEY (code_toAirport) REFERENCES `Airport` (`code`);
ALTER TABLE `FlightTemplate` ADD FOREIGN KEY (id_Connection) REFERENCES `Connection` (`id`);
ALTER TABLE `AirlineCompany` ADD FOREIGN KEY (code) REFERENCES `FlightTemplate` (`code_AirlineCompany`);
ALTER TABLE `Flight` ADD FOREIGN KEY (id_FlightTemplate) REFERENCES `FlightTemplate` (`id`);
ALTER TABLE `Flight` ADD FOREIGN KEY (code_AirplaneModel) REFERENCES `AirplaneModel` (`id`);
ALTER TABLE `AirplaneModel` ADD FOREIGN KEY (id_Manufacturer) REFERENCES `Manufacturer` (`id`);
ALTER TABLE `SeatPricing` ADD FOREIGN KEY (id_SeatType) REFERENCES `SeatType` (`id`);
ALTER TABLE `SeatPricing` ADD FOREIGN KEY (id_Flight) REFERENCES `Flight` (`id`);

-- ---
-- Table Properties
-- ---

-- ALTER TABLE `City` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `Airport` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `Country` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `Connection` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `FlightTemplate` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `AirlineCompany` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `SeatType` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `Flight` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `AirplaneModel` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `Manufacturer` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `SeatPricing` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ---
-- Test Data
-- ---

-- INSERT INTO `City` (`id`,`name`,`id_Country`) VALUES
-- ('','','');
-- INSERT INTO `Airport` (`code`,`name`,`id_City`) VALUES
-- ('','','');
-- INSERT INTO `Country` (`id`,`name`) VALUES
-- ('','');
-- INSERT INTO `Connection` (`id`,`code_fromAirport`,`code_toAirport`,`distance`) VALUES
-- ('','','','');
-- INSERT INTO `FlightTemplate` (`id`,`code`,`code_AirlineCompany`,`id_Connection`) VALUES
-- ('','','','');
-- INSERT INTO `AirlineCompany` (`code`,`name`) VALUES
-- ('','');
-- INSERT INTO `SeatType` (`id`,`name`) VALUES
-- ('','');
-- INSERT INTO `Flight` (`id`,`id_FlightTemplate`,`time`,`code_AirplaneModel`) VALUES
-- ('','','','');
-- INSERT INTO `AirplaneModel` (`id`,`name`,`cruiseSpeed`,`maxNbOfSeats`,`id_Manufacturer`) VALUES
-- ('','','','','');
-- INSERT INTO `Manufacturer` (`id`,`name`) VALUES
-- ('','');
-- INSERT INTO `SeatPricing` (`id`,`id_SeatType`,`id_Flight`,`price`,`nbSeats`) VALUES
-- ('','','','','');
