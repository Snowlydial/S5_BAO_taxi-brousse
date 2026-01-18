\c taxibroussedb;

INSERT INTO ClassePlace (libelle, prix_place) VALUES 
('Standard', 80000),
('Premium', 140000),
('VIP', 180000);

INSERT INTO Caisse (libelle) VALUES 
('Espece'),
('Mobile Money (MVola)'),
('Carte Bancaire');

INSERT INTO CategorieGenre (libelle) VALUES 
('Homme'),
('Femme');

INSERT INTO CategorieGroupeAge (libelle) VALUES 
('Enfant (0-12 ans)'),
('Adulte (18-59 ans)'),
('Senior (60+ ans)');

-- ADULTE prices
INSERT INTO ClasseAge_Conf (valeur_override, est_pourcentage, date_debut, date_fin, id_categoriegroupeage, id_classeplace) 
VALUES 
(50000, FALSE, NULL, NULL, 2, 1),
(60000, FALSE, NULL, NULL, 2, 2),
(70000, FALSE, NULL, NULL, 2, 3);

-- ENFANT pricing
INSERT INTO ClasseAge_Conf (valeur_override, est_pourcentage, date_debut, date_fin, id_categoriegroupeage, id_classeplace) 
VALUES 
(40000, FALSE, NULL, NULL, 1, 1),
(50000, FALSE, NULL, NULL, 1, 2),
(65000, FALSE, NULL, NULL, 1, 3);

-- SENIOR pricing 
INSERT INTO ClasseAge_Conf (valeur_override, est_pourcentage, date_debut, date_fin, id_categoriegroupeage, id_classeplace) 
VALUES 
(-0.20, TRUE, NULL, NULL, 3, 1),
(-0.20, TRUE, NULL, NULL, 3, 2),
(-0.20, TRUE, NULL, NULL, 3, 3);

-- Gare
INSERT INTO Gare (libelle) VALUES 
('Fasan''karana'),
('Ambolomadinika'),
('Analakely'),
('Toamasina Gare Routiere'),
('Antsirabe Gare Routiere'),
('Fianarantsoa Gare Routiere'),
('Toamasina'),
('Antananarivo'),
('Tamatave');

INSERT INTO BusConf (libelle, valeur) VALUES 
('nb_place_standard', '30'),
('nb_place_standard', '25'),
('nb_place_standard', '20'),
('nb_place_VIP', '15'),
('nb_place_premium', '15'),
('nb_place_standard', '15'),
('climatisation', 'oui'),
('climatisation', 'non'),
('wifi', 'oui'),
('wifi', 'non');

INSERT INTO Bus (immatriculation) VALUES 
('1234 TAA'),
('3456 TAC'),
('5678 TAE'),
('TB-2026');

INSERT INTO Bus_BusConf (id_bus, id_busConf) VALUES 
-- Bus 1: 30 standard places, no AC, no wifi
(1, 1), (1, 8), (1, 10),
-- Bus 2: 25 standard places, AC, no wifi
(2, 2), (2, 7), (2, 10),
-- Bus 3: 20 standard places, AC, wifi
(3, 3), (3, 7), (3, 9),
-- Bus 4: Mixed (2 VIP, 6 Premium, 10 Standard)
(4, 4), (4, 5), (4, 6);

INSERT INTO Voyage (duree, prix_voyage, id_gare_1, id_gare_2) VALUES 
(0.5, 30000.00, 1, 2),        -- Fasan'karana -> Ambolomadinika
(8.0, 90000.00, 8, 9);    -- Tana -> Tamatave

INSERT INTO Bus_Voyage (heure_depart, date_depart, prix_specifique, id_bus, id_voyage) VALUES 
('06:00:00', '2026-01-20', NULL, 1, 1),
('07:00:00', '2026-01-27', NULL, 4, 2);

INSERT INTO Client (nom, prenom, id_categorieGenre, id_categorieGroupeAge) VALUES 
('Enfant1', 'Test', 1, 1),
('Enfant2', 'Test', 2, 1),
('Adulte1', 'Test', 1, 2),
('Adulte2', 'Test', 2, 2),
('Senior1', 'Test', 1, 3),
('Senior2', 'Test', 2, 3);

--? Alea update the price conf of adulte
-- UPDATE ClasseAge_Conf
-- SET valeur_override = 15000
-- WHERE id_categorieGroupeAge = 2 AND id_classePlace = 1;

-- UPDATE ClasseAge_Conf
-- SET valeur_override = 20000
-- WHERE id_categorieGroupeAge = 2 AND id_classePlace = 2;

-- UPDATE ClasseAge_Conf
-- SET valeur_override = 25000
-- WHERE id_categorieGroupeAge = 2 AND id_classePlace = 3;