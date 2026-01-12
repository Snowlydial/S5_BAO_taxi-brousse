DROP IF EXISTS DATABASE taxibroussedb;
CREATE DATABASE taxibroussedb;
\c taxibroussedb;

--? TABLES
CREATE TABLE Caisse(
   id SERIAL,
   label VARCHAR(50) ,
   PRIMARY KEY(id)
);

CREATE TABLE CategorieGenre(
   id SERIAL,
   libelle VARCHAR(50) ,
   PRIMARY KEY(id)
);

CREATE TABLE CategorieGroupeAge(
   id SERIAL,
   libelle VARCHAR(50) ,
   PRIMARY KEY(id)
);

CREATE TABLE Gare(
   id SERIAL,
   libelle VARCHAR(50) ,
   PRIMARY KEY(id)
);

CREATE TABLE BusClasse(
   id SERIAL,
   libelle VARCHAR(50) ,
   prix_additif DOUBLE PRECISION,
   PRIMARY KEY(id)
);

CREATE TABLE BusConf(
   id SERIAL,
   libelle VARCHAR(50) ,
   valeur VARCHAR(255) ,
   PRIMARY KEY(id)
);

CREATE TABLE HistoriquePrixClasse(
   id SERIAL,
   date_ecriture TIMESTAMP,
   id_busclasse INTEGER NOT NULL,
   PRIMARY KEY(id),
   FOREIGN KEY(id_busclasse) REFERENCES BusClasse(id)
);

CREATE TABLE Bus(
   id SERIAL,
   immatriculation VARCHAR(50) ,
   id_busclasse INTEGER NOT NULL,
   PRIMARY KEY(id),
   UNIQUE(immatriculation),
   FOREIGN KEY(id_busclasse) REFERENCES BusClasse(id)
);

CREATE TABLE Voyage(
   id SERIAL,
   duree DOUBLE PRECISION,
   prix_additif DOUBLE PRECISION,
   id_garedepart INTEGER NOT NULL,
   id_garearrivee INTEGER NOT NULL,
   PRIMARY KEY(id),
   FOREIGN KEY(id_garedepart) REFERENCES Gare(id),
   FOREIGN KEY(id_garearrivee) REFERENCES Gare(id)
);

CREATE TABLE Client(
   id SERIAL,
   nom VARCHAR(50) ,
   prenom VARCHAR(50) ,
   id_categoriegenre INTEGER NOT NULL,
   id_categoriegroupeage INTEGER NOT NULL,
   PRIMARY KEY(id),
   FOREIGN KEY(id_categoriegenre) REFERENCES CategorieGenre(id),
   FOREIGN KEY(id_categoriegroupeage) REFERENCES CategorieGroupeAge(id)
);

CREATE TABLE HistoriquePrixVoyage(
   id SERIAL,
   date_ecriture TIMESTAMP,
   id_voyage INTEGER NOT NULL,
   PRIMARY KEY(id),
   FOREIGN KEY(id_voyage) REFERENCES Voyage(id)
);

CREATE TABLE Bus_Voyage(
   id SERIAL,
   heure_depart TIME,
   date_depart DATE,
   id_bus INTEGER NOT NULL,
   id_voyage INTEGER NOT NULL,
   PRIMARY KEY(id),
   FOREIGN KEY(id_bus) REFERENCES Bus(id),
   FOREIGN KEY(id_voyage) REFERENCES Voyage(id)
);

CREATE TABLE Reservation(
   id SERIAL,
   id_client INTEGER NOT NULL,
   id_busvoyage INTEGER NOT NULL,
   PRIMARY KEY(id),
   FOREIGN KEY(id_client) REFERENCES Client(id),
   FOREIGN KEY(id_busvoyage) REFERENCES Bus_Voyage(id)
);

CREATE TABLE Paiement(
   id SERIAL,
   date_paiement TIMESTAMP,
   montant_paye DOUBLE PRECISION,
   id_caisse INTEGER NOT NULL,
   id_reservation INTEGER NOT NULL,
   PRIMARY KEY(id),
   FOREIGN KEY(id_caisse) REFERENCES Caisse(id),
   FOREIGN KEY(id_reservation) REFERENCES Reservation(id)
);

CREATE TABLE Bus_BusConf(
   id SERIAL,
   id_bus INTEGER,
   id_busconf INTEGER,
   PRIMARY KEY(id),
   FOREIGN KEY(id_bus) REFERENCES Bus(id),
   FOREIGN KEY(id_busconf) REFERENCES BusConf(id)
);