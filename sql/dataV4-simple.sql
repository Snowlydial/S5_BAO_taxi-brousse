\c taxibroussedb;

--?=== Base Configuration Data

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

--?=== Gares

INSERT INTO Gare (libelle) VALUES 
('Antananarivo'),
('Toamasina'),
('Majunga');

--?=== Bus Configuration

INSERT INTO BusConf (libelle, valeur) VALUES 
('nb_place_standard', '100'),
('climatisation', 'oui'),
('wifi', 'oui');

--?=== Bus

INSERT INTO Bus (immatriculation) VALUES ('TBK1224');

-- Associate Bus with configuration (100 standard places, AC, wifi)
INSERT INTO Bus_BusConf (id_bus, id_busConf) VALUES 
(1, 1),  -- 100 standard places
(1, 2),  -- AC
(1, 3);  -- wifi

--?=== Voyages

INSERT INTO Voyage (duree, prix_voyage, id_gare_1, id_gare_2) VALUES 
(2.0, 40000.00, 1, 2);  -- TNR -> Toamasina (2h)

--?=== Bus Voyages

INSERT INTO Bus_Voyage (heure_depart, date_depart, prix_specifique, id_bus, id_voyage) VALUES
('10:00:00', '2026-01-20', NULL, 1, 1),  -- 1er busVoyage: 20-01-26, 10h
('10:00:00', '2026-01-21', NULL, 1, 1),  -- 2eme busVoyage: 21-01-26, 10h
('15:00:00', '2026-01-21', NULL, 1, 1);  -- 3eme busVoyage: 21-01-26, 15h

--?=== Clients (sample adults for reservations)

INSERT INTO Client (nom, prenom, id_categorieGenre, id_categorieGroupeAge) VALUES 
('Adulte1', 'Test', 1, 2),
('Adulte2', 'Test', 2, 2),
('Adulte3', 'Test', 1, 2);

--?=== Diffusion Configuration

INSERT INTO DiffusionConf (prix, date_debut, date_fin) VALUES 
(100000, '2026-01-01', '2026-12-31');

--?=== Societes

INSERT INTO Societe (nom) VALUES 
('Vaniala'),
('Lewis'),
('Socobis'),
('Jejoo');

--?=== 1er Bus Voyage: 40 Reservations + 2 Diffusions (Vaniala, Lewis)

-- 40 Reservations (places 1-40)
INSERT INTO Reservation (numero_place, id_classeplace, id_client, id_bus_voyage)
SELECT 
    gs.seat_num,
    1,  -- Standard
    1,  -- Client Adulte1
    1   -- 1er bus_voyage
FROM generate_series(1, 40) AS gs(seat_num);

-- ReservationStatut for 40 reservations
INSERT INTO ReservationStatut (id_reservation, date_annulation)
SELECT id_reservation, NULL::DATE
FROM Reservation 
WHERE id_bus_voyage = 1;

-- Paiements for 40 reservations
INSERT INTO Paiement (date_paiement, montant_paye, id_caisse, id_reservation)
SELECT 
    '2026-01-19 14:00:00'::TIMESTAMP,
    50000.00,  -- Adult standard price
    1,  -- Espece
    id_reservation
FROM Reservation 
WHERE id_bus_voyage = 1;

-- 1 Diffusion Vaniala
INSERT INTO Diffusion (date_diffusion, heure_diffusion, id_societe, id_bus_voyage, description) VALUES
('2026-01-20', '09:00:00', 1, 1, 'Pub Vaniala - 1er voyage');

-- 1 Diffusion Lewis
INSERT INTO Diffusion (date_diffusion, heure_diffusion, id_societe, id_bus_voyage, description) VALUES
('2026-01-20', '09:30:00', 2, 1, 'Pub Lewis - 1er voyage');

--?=== 2eme Bus Voyage: 30 Reservations + 3 Diffusions (2 Socobis, 1 Jejoo)

-- 30 Reservations (places 1-30)
INSERT INTO Reservation (numero_place, id_classeplace, id_client, id_bus_voyage)
SELECT 
    gs.seat_num,
    1,
    2,  -- Client Adulte2
    2   -- 2eme bus_voyage
FROM generate_series(1, 30) AS gs(seat_num);

-- ReservationStatut for 30 reservations
INSERT INTO ReservationStatut (id_reservation, date_annulation)
SELECT id_reservation, NULL::DATE
FROM Reservation 
WHERE id_bus_voyage = 2;

-- Paiements for 30 reservations
INSERT INTO Paiement (date_paiement, montant_paye, id_caisse, id_reservation)
SELECT 
    '2026-01-20 16:00:00'::TIMESTAMP,
    50000.00,
    1,
    id_reservation
FROM Reservation 
WHERE id_bus_voyage = 2;

-- 2 Diffusions Socobis
INSERT INTO Diffusion (date_diffusion, heure_diffusion, id_societe, id_bus_voyage, description) VALUES
('2026-01-21', '09:00:00', 3, 2, 'Pub Socobis - 2eme voyage #1'),
('2026-01-21', '09:15:00', 3, 2, 'Pub Socobis - 2eme voyage #2');

-- 1 Diffusion Jejoo
INSERT INTO Diffusion (date_diffusion, heure_diffusion, id_societe, id_bus_voyage, description) VALUES
('2026-01-21', '09:30:00', 4, 2, 'Pub Jejoo - 2eme voyage');

--?=== 3eme Bus Voyage: 50 Reservations + 0 Diffusions

-- 50 Reservations (places 1-50)
INSERT INTO Reservation (numero_place, id_classeplace, id_client, id_bus_voyage)
SELECT 
    gs.seat_num,
    1,
    3,  -- Client Adulte3
    3   -- 3eme bus_voyage
FROM generate_series(1, 50) AS gs(seat_num);

-- ReservationStatut for 50 reservations
INSERT INTO ReservationStatut (id_reservation, date_annulation)
SELECT id_reservation, NULL::DATE
FROM Reservation 
WHERE id_bus_voyage = 3;

-- Paiements for 50 reservations
INSERT INTO Paiement (date_paiement, montant_paye, id_caisse, id_reservation)
SELECT 
    '2026-01-21 10:00:00'::TIMESTAMP,
    50000.00,
    1,
    id_reservation
FROM Reservation 
WHERE id_bus_voyage = 3;

--?=== Summary
-- 1er Bus Voyage (20-01-26 10h): 40 reservations, 2 diffusions (Vaniala, Lewis)
-- 2eme Bus Voyage (21-01-26 10h): 30 reservations, 3 diffusions (2 Socobis, 1 Jejoo)
-- 3eme Bus Voyage (21-01-26 15h): 50 reservations, 0 diffusions
-- 
-- No DiffusionPaiement inserted - to be done manually via UI