DROP DATABASE IF EXISTS taxibroussedb;
CREATE DATABASE taxibroussedb;
\c taxibroussedb;

--? TABLES
CREATE TABLE Caisse(
   id_caisse SERIAL,
   libelle VARCHAR(50) ,
   PRIMARY KEY(id_caisse)
);

CREATE TABLE CategorieGenre(
   id_categorieGenre SERIAL,
   libelle VARCHAR(50) ,
   PRIMARY KEY(id_categorieGenre)
);

CREATE TABLE CategorieGroupeAge(
   id_categorieGroupeAge SERIAL,
   libelle VARCHAR(50) ,
   PRIMARY KEY(id_categorieGroupeAge)
);

CREATE TABLE Gare(
   id_gare SERIAL,
   libelle VARCHAR(50) ,
   PRIMARY KEY(id_gare)
);

CREATE TABLE BusClasse(
   id_busClasse SERIAL,
   libelle VARCHAR(50) ,
   prix_classe DOUBLE PRECISION,
   PRIMARY KEY(id_busClasse)
);

CREATE TABLE BusConf(
   id_busConf SERIAL,
   libelle VARCHAR(50) ,
   valeur VARCHAR(255) ,
   PRIMARY KEY(id_busConf)
);

CREATE TABLE HistoriquePrixClasse(
   id_histoPrixClasse SERIAL,
   date_ecriture TIMESTAMP,
   prix_classe DOUBLE PRECISION,
   id_busClasse INTEGER NOT NULL,
   PRIMARY KEY(id_histoPrixClasse),
   FOREIGN KEY(id_busClasse) REFERENCES BusClasse(id_busClasse)
);

CREATE TABLE Bus(
   id_bus SERIAL,
   immatriculation VARCHAR(50) ,
   id_busClasse INTEGER NOT NULL,
   PRIMARY KEY(id_bus),
   UNIQUE(immatriculation),
   FOREIGN KEY(id_busClasse) REFERENCES BusClasse(id_busClasse)
);

CREATE TABLE Voyage(
   id_voyage SERIAL,
   duree DOUBLE PRECISION,
   prix_voyage DOUBLE PRECISION,
   id_gare_1 INTEGER NOT NULL,
   id_gare_2 INTEGER NOT NULL,
   PRIMARY KEY(id_voyage),
   FOREIGN KEY(id_gare_1) REFERENCES Gare(id_gare),
   FOREIGN KEY(id_gare_2) REFERENCES Gare(id_gare)
);

CREATE TABLE Client(
   id_client SERIAL,
   nom VARCHAR(50) ,
   prenom VARCHAR(50) ,
   id_categorieGenre INTEGER NOT NULL,
   id_categorieGroupeAge INTEGER NOT NULL,
   PRIMARY KEY(id_client),
   FOREIGN KEY(id_categorieGenre) REFERENCES CategorieGenre(id_categorieGenre),
   FOREIGN KEY(id_categorieGroupeAge) REFERENCES CategorieGroupeAge(id_categorieGroupeAge)
);

CREATE TABLE HistoriquePrixVoyage(
   id_histoPrixVoyage SERIAL,
   date_ecriture TIMESTAMP,
   prix_voyage DOUBLE PRECISION,
   id_voyage INTEGER NOT NULL,
   PRIMARY KEY(id_histoPrixVoyage),
   FOREIGN KEY(id_voyage) REFERENCES Voyage(id_voyage)
);

CREATE TABLE Bus_Voyage(
   id_bus_voyage SERIAL,
   heure_depart TIME,
   date_depart DATE,
   prix_specifique DOUBLE PRECISION,
   id_bus INTEGER NOT NULL,
   id_voyage INTEGER NOT NULL,
   PRIMARY KEY(id_bus_voyage),
   FOREIGN KEY(id_bus) REFERENCES Bus(id_bus),
   FOREIGN KEY(id_voyage) REFERENCES Voyage(id_voyage)
);

CREATE TABLE HistoriquePrixSpecifique(
   id_histoPrixSpecifique SERIAL,
   date_ecriture TIMESTAMP,
   prix_specifique DOUBLE PRECISION,
   id_bus_voyage INTEGER NOT NULL,
   PRIMARY KEY(id_histoPrixSpecifique),
   FOREIGN KEY(id_bus_voyage) REFERENCES Bus_Voyage(id_bus_voyage)
);

CREATE TABLE Reservation(
   id_reservation SERIAL,
   numero_place INTEGER,
   id_client INTEGER NOT NULL,
   id_bus_voyage INTEGER NOT NULL,
   PRIMARY KEY(id_reservation),
   FOREIGN KEY(id_client) REFERENCES Client(id_client),
   FOREIGN KEY(id_bus_voyage) REFERENCES Bus_Voyage(id_bus_voyage)
);

CREATE TABLE Paiement(
   id_paiement SERIAL,
   date_paiement TIMESTAMP,
   montant_paye DOUBLE PRECISION,
   id_caisse INTEGER NOT NULL,
   id_reservation INTEGER NOT NULL,
   PRIMARY KEY(id_paiement),
   FOREIGN KEY(id_caisse) REFERENCES Caisse(id_caisse),
   FOREIGN KEY(id_reservation) REFERENCES Reservation(id_reservation)
);

CREATE TABLE ReservationStatut(
   id_reservationStatut SERIAL,
   date_annulation DATE,
   id_reservation INTEGER NOT NULL,
   PRIMARY KEY(id_reservationStatut),
   FOREIGN KEY(id_reservation) REFERENCES Reservation(id_reservation)
);

CREATE TABLE Bus_BusConf(
   id_bus_busConf SERIAL,
   id_bus INTEGER,
   id_busConf INTEGER,
   PRIMARY KEY(id_bus_busConf),
   FOREIGN KEY(id_bus) REFERENCES Bus(id_bus),
   FOREIGN KEY(id_busConf) REFERENCES BusConf(id_busConf)
);
