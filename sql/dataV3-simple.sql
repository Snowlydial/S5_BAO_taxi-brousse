\c taxibroussedb;

-- ClassePlace (Seat classes with their prices)
INSERT INTO ClassePlace (libelle, prix_place) VALUES 
('Premium', 140000),
('VIP', 180000),
('Standard', 80000);

-- Caisse (Payment methods)
INSERT INTO Caisse (libelle) VALUES 
('Espece'),
('Mobile Money (MVola)'),
('Carte Bancaire');

-- CategorieGenre
INSERT INTO CategorieGenre (libelle) VALUES 
('Homme'),
('Femme');

-- CategorieGroupeAge
INSERT INTO CategorieGroupeAge (libelle, prix_standard_override) VALUES 
('Enfant (0-12 ans)', 50000),
('Adulte (18-59 ans)', NULL),
('Senior (60+ ans)', -0.20);

-- Gare
INSERT INTO Gare (libelle) VALUES 
('Fasan''karana'),
('Ambolomadinika'),
('Analakely'),
('Toamasina Gare Routiere'),
('Antsirabe Gare Routiere'),
('Fianarantsoa Gare Routiere');

-- BusClasse
INSERT INTO BusClasse (libelle, prix_classe) VALUES 
('Economique', 12000.00),
('Standard', 22000.00),
('Confort Plus', 35000.00);

-- BusConf (Configuration types - REMOVED all 'capacite' entries)
INSERT INTO BusConf (libelle, valeur) VALUES 
('nb_place_standard', '30'),  -- For Bus 1 (Economique - 30 places all Standard)
('nb_place_standard', '25'),  -- For Bus 2 (Standard - 25 places all Standard)
('nb_place_standard', '20'),  -- For Bus 3 (Confort Plus - 20 places all Standard)
('climatisation', 'oui'),
('climatisation', 'non'),
('wifi', 'oui'),
('wifi', 'non');

-- Bus
INSERT INTO Bus (immatriculation, id_busClasse) VALUES 
('1234 TAA', 1), -- Economique
('3456 TAC', 2), -- Standard
('5678 TAE', 3); -- Confort Plus

-- Bus_BusConf (Link buses to configurations - using only nb_place_standard for all buses)
INSERT INTO Bus_BusConf (id_bus, id_busConf) VALUES 
-- Bus 1: Economique (30 standard places, no AC, no wifi)
(1, 1), (1, 5), (1, 7),  -- Bus 1: 30 standard, climatisation=non, wifi=non
(2, 2), (2, 4), (2, 7),  -- Bus 2: Standard (25 standard places, AC, no wifi) -- 25 standard, climatisation=oui, wifi=non
(3, 3), (3, 4), (3, 6);  -- Bus 3: Confort Plus (20 standard places, AC, wifi)

-- Voyage (Routes)
INSERT INTO Voyage (duree, prix_voyage, id_gare_1, id_gare_2) VALUES 
(0.5, NULL, 1, 2),        -- Local: Fasan'karana -> Ambolomadinika (uses prix_classe)
(1.0, NULL, 2, 3),        -- Local: Ambolomadinika -> Analakely (uses prix_classe)
(8.0, 35000.00, 1, 4),    -- Tana -> Toamasina (specific prix_voyage)
(3.5, 18000.00, 1, 5),    -- Tana -> Antsirabe (specific prix_voyage)
(12.0, 65000.00, 1, 6);   -- Tana -> Fianarantsoa (specific prix_voyage)

-- Bus_Voyage (January-February 2026 only)
INSERT INTO Bus_Voyage (heure_depart, date_depart, prix_specifique, id_bus, id_voyage) VALUES 
-- JANUARY 2026
('06:00:00', '2026-01-15', NULL, 1, 1),   -- Local Economique
('08:00:00', '2026-01-16', NULL, 2, 1),   -- Local Standard
('10:00:00', '2026-01-18', NULL, 3, 2),   -- Local Confort
('05:00:00', '2026-01-20', NULL, 1, 3),   -- Toamasina Economique
('06:00:00', '2026-01-22', NULL, 2, 3),   -- Toamasina Standard
('07:00:00', '2026-01-25', NULL, 3, 3),   -- Toamasina Confort
('06:00:00', '2026-01-28', NULL, 2, 4),   -- Antsirabe
('04:00:00', '2026-01-30', NULL, 3, 5),   -- Fianarantsoa
('06:00:00', '2026-02-05', NULL, 1, 1),   -- Local
('05:00:00', '2026-02-10', NULL, 1, 3),   -- Toamasina
('06:00:00', '2026-02-15', NULL, 2, 4),   -- Antsirabe
('04:00:00', '2026-02-20', 80000.00, 3, 5); -- Fianarantsoa (special price)

-- Client
INSERT INTO Client (nom, prenom, id_categorieGenre, id_categorieGroupeAge) VALUES 
('Rakoto', 'Jean', 1, 2),
('Rasoa', 'Marie', 2, 2),
('Randria', 'Paul', 1, 2),
('Raharison', 'Sophie', 2, 2),
('Andrianina', 'Luc', 1, 2),
('Raharimanana', 'Michel', 1, 3),
('Rakotomalala', 'Nicole', 2, 3),
('Rabetsitonta', 'Kanto', 1, 1);

--? Alea week 2:
INSERT INTO Gare (libelle) VALUES 
('Toamasina'),
('Antananarivo');

INSERT INTO Voyage (duree, prix_voyage, id_gare_1, id_gare_2) VALUES 
(8.0, 40000.00, 8, 7);

INSERT INTO Bus (immatriculation, id_busClasse) VALUES 
('nyeh', 2);

-- BusConf for mixed place types (VIP, Premium, Standard)
INSERT INTO BusConf (libelle, valeur) VALUES 
('nb_place_VIP', '2'),
('nb_place_premium', '6'),
('nb_place_standard', '10');

INSERT INTO Bus_BusConf (id_bus, id_busConf) VALUES 
(4, 8),  -- nb_place_VIP: 2 (id = 7 from new insert)
(4, 9),  -- nb_place_premium: 6 (id = 8)
(4, 10);  -- nb_place_standard: 10 (id = 9)

INSERT INTO Bus_Voyage (heure_depart, date_depart, prix_specifique, id_bus, id_voyage) VALUES 
('07:00:00', '2026-01-25', NULL, 4, 6);

--? Alea week 2b:
INSERT INTO Client (nom, prenom, id_categorieGenre, id_categorieGroupeAge) VALUES 
('Enfant', 'Test', 2, 1);
INSERT INTO Client (nom, prenom, id_categorieGenre, id_categorieGroupeAge) VALUES 
('Senior', 'Test', 2, 3);

INSERT INTO Gare (libelle) VALUES 
('Tamatave');

INSERT INTO Voyage (duree, prix_voyage, id_gare_1, id_gare_2) VALUES 
(8.0, 40000.00, 8, 9);

INSERT INTO Bus_Voyage (heure_depart, date_depart, prix_specifique, id_bus, id_voyage) VALUES 
('07:00:00', '2026-01-27', NULL, 4, 7);

