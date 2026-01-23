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

--? Alea Week 3
INSERT INTO Societe (nom) VALUES ('Vaniala'), ('Lewis');

INSERT INTO DiffusionConf (prix, date_debut, date_fin) VALUES (100000, '2025-12-01', '2025-12-31');

INSERT INTO Bus_Voyage (heure_depart, date_depart, prix_specifique, id_bus, id_voyage) VALUES
('08:00:00', '2025-12-01', NULL, 1, 1),
('10:00:00', '2025-12-21', NULL, 4, 2);

-- Insert 20 diffusions for Vaniala: 2025-12-01 -> 2025-12-20
-- INSERT INTO Diffusion (date_diffusion, heure_diffusion, id_societe, id_bus_voyage, description)
-- SELECT d::date, '09:00:00', (SELECT id_societe FROM Societe WHERE nom = 'Vaniala' LIMIT 1),
-- 	   (SELECT id_bus_voyage FROM Bus_Voyage WHERE date_depart = '2025-12-01' LIMIT 1),
-- 	   'Publicite Vaniala'
-- FROM generate_series('2025-12-01'::date, '2025-12-20'::date, '1 day') AS d;

-- -- Insert 10 diffusions for Lewis: 2025-12-21 -> 2025-12-30
-- INSERT INTO Diffusion (date_diffusion, heure_diffusion, id_societe, id_bus_voyage, description)
-- SELECT d::date, '11:00:00', (SELECT id_societe FROM Societe WHERE nom = 'Lewis' LIMIT 1),
-- 	   (SELECT id_bus_voyage FROM Bus_Voyage WHERE date_depart = '2025-12-21' LIMIT 1),
-- 	   'Publicite Lewis'
-- FROM generate_series('2025-12-21'::date, '2025-12-30'::date, '1 day') AS d;


-- Add to dataV3-simple.sql at the end

--?=== Alea week 3 suite

-- New Bus
INSERT INTO Bus (immatriculation) VALUES ('TBK1224');

-- New BusConf for 100 standard places
INSERT INTO BusConf (libelle, valeur) VALUES ('nb_place_standard', '100');

-- Associate Bus with BusConf (Bus id=5, BusConf id will be auto-generated, let's say 11)
INSERT INTO Bus_BusConf (id_bus, id_busConf) VALUES 
(5, 11), (5, 7), (5, 9); -- 100 standard, AC, wifi

-- New Societes
INSERT INTO Societe (nom) VALUES ('Socobis'), ('Jejoo');

-- New Voyages
INSERT INTO Voyage (duree, prix_voyage, id_gare_1, id_gare_2) VALUES 
(2.0, 40000.00, 8, 7),  -- TNR -> Toamasina (2h)
(2.0, 45000.00, 8, 9);  -- TNR -> Majunga (assuming gare 9 exists or create new)

-- If Majunga doesn't exist, create it:
INSERT INTO Gare (libelle) VALUES ('Majunga');

-- Update voyage reference (TNR is gare 8, Majunga is now gare 10)
UPDATE Voyage SET id_gare_2 = 10 WHERE id_voyage = (SELECT MAX(id_voyage) FROM Voyage);

-- New Bus_Voyages with the new bus (id=5)
INSERT INTO Bus_Voyage (heure_depart, date_depart, prix_specifique, id_bus, id_voyage) VALUES
('10:00:00', '2026-01-20', NULL, 5, 3),  -- 1er busVoyage: 20-01-26, 10h (TNR-Toamasina)
('10:00:00', '2026-01-21', NULL, 5, 3),  -- 2eme busVoyage: 21-01-26, 10h (TNR-Toamasina)
('15:00:00', '2026-01-21', NULL, 5, 3);  -- 3eme busVoyage: 21-01-26, 15h (TNR-Toamasina)

-- Get the bus_voyage IDs for reference
-- 1er: will be id 5
-- 2eme: will be id 6
-- 3eme: will be id 7

-- 40 Reservations for 1er busVoyage (standard places 1-40)
INSERT INTO Reservation (numero_place, id_classeplace, id_client, id_bus_voyage)
SELECT 
    gs.seat_num,
    1,  -- Standard ClassePlace
    (SELECT id_client FROM Client WHERE id_categoriegroupeage = 2 LIMIT 1),  -- Adult client
    5   -- 1er bus_voyage
FROM generate_series(1, 40) AS gs(seat_num);

-- Create ReservationStatut for these 40 reservations
INSERT INTO ReservationStatut (id_reservation, date_annulation)
SELECT id_reservation, NULL::DATE
FROM Reservation 
WHERE id_bus_voyage = 5;

-- Create Paiements for these 40 reservations
INSERT INTO Paiement (date_paiement, montant_paye, id_caisse, id_reservation)
SELECT 
    '2026-01-19 14:00:00'::TIMESTAMP,
    50000.00,  -- Adult standard price
    1,  -- Espece
    id_reservation
FROM Reservation 
WHERE id_bus_voyage = 5;

-- 1 Diffusion Vaniala for 1er busVoyage
INSERT INTO Diffusion (date_diffusion, heure_diffusion, id_societe, id_bus_voyage, description) VALUES
('2026-01-20', '09:00:00', 1, 5, 'Pub Vaniala - 1er voyage');

-- 1 Diffusion Lewis for 1er busVoyage
INSERT INTO Diffusion (date_diffusion, heure_diffusion, id_societe, id_bus_voyage, description) VALUES
('2026-01-20', '09:30:00', 2, 5, 'Pub Lewis - 1er voyage');

-- 30 Reservations for 2eme busVoyage (standard places 1-30)
INSERT INTO Reservation (numero_place, id_classeplace, id_client, id_bus_voyage)
SELECT 
    gs.seat_num,
    1,
    (SELECT id_client FROM Client WHERE id_categoriegroupeage = 2 LIMIT 1),
    6
FROM generate_series(1, 30) AS gs(seat_num);

-- Create ReservationStatut for these 30 reservations
INSERT INTO ReservationStatut (id_reservation, date_annulation)
SELECT id_reservation, NULL::DATE
FROM Reservation 
WHERE id_bus_voyage = 6;

-- Create Paiements for these 30 reservations
INSERT INTO Paiement (date_paiement, montant_paye, id_caisse, id_reservation)
SELECT 
    '2026-01-20 16:00:00'::TIMESTAMP,
    50000.00,
    1,
    id_reservation
FROM Reservation 
WHERE id_bus_voyage = 6;

-- 2 Diffusions Socobis for 2eme busVoyage
INSERT INTO Diffusion (date_diffusion, heure_diffusion, id_societe, id_bus_voyage, description) VALUES
('2026-01-21', '09:00:00', 3, 6, 'Pub Socobis - 2eme voyage #1'),
('2026-01-21', '09:15:00', 3, 6, 'Pub Socobis - 2eme voyage #2');

-- 1 Diffusion Jejoo for 2eme busVoyage
INSERT INTO Diffusion (date_diffusion, heure_diffusion, id_societe, id_bus_voyage, description) VALUES
('2026-01-21', '09:30:00', 4, 6, 'Pub Jejoo - 2eme voyage');

-- 50 Reservations for 3eme busVoyage (standard places 1-50)
INSERT INTO Reservation (numero_place, id_classeplace, id_client, id_bus_voyage)
SELECT 
    gs.seat_num,
    1,
    (SELECT id_client FROM Client WHERE id_categoriegroupeage = 2 LIMIT 1),
    7
FROM generate_series(1, 50) AS gs(seat_num);

-- Create ReservationStatut for these 50 reservations
INSERT INTO ReservationStatut (id_reservation, date_annulation)
SELECT id_reservation, NULL::DATE
FROM Reservation 
WHERE id_bus_voyage = 7;

-- Create Paiements for these 50 reservations
INSERT INTO Paiement (date_paiement, montant_paye, id_caisse, id_reservation)
SELECT 
    '2026-01-21 10:00:00'::TIMESTAMP,
    50000.00,
    1,
    id_reservation
FROM Reservation 
WHERE id_bus_voyage = 7;

-- 0 Diffusions for 3eme busVoyage (no insert needed)