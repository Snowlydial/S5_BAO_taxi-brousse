\c taxibroussedb;

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
INSERT INTO CategorieGroupeAge (libelle) VALUES 
('Enfant (0-12 ans)'),
('Adulte (18-59 ans)'),
('Senior (60+ ans)');

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

-- BusConf (Configuration types)
INSERT INTO BusConf (libelle, valeur) VALUES 
('capacite', '30'),
('capacite', '25'),
('capacite', '20'),
('nb_place_premium', '10'),
('nb_place_premium', '8'),
('nb_place_premium', '6'),
('nb_place_standard', '20'),
('nb_place_standard', '17'),
('nb_place_standard', '14'),
('nb_place_standard', '14'),
('climatisation', 'oui'),
('climatisation', 'non'),
('wifi', 'oui'),
('wifi', 'non');

-- Bus
INSERT INTO Bus (immatriculation, id_busClasse) VALUES 
('1234 TAA', 1), -- Economique
('3456 TAC', 2), -- Standard
('5678 TAE', 3); -- Confort Plus

-- Bus_BusConf (Link buses to configurations)
INSERT INTO Bus_BusConf (id_bus, id_busConf) VALUES 
-- Bus 1: Economique (30 places: 10 premium + 20 standard, no AC, no wifi)
(1, 1), (1, 4), (1, 7), (1, 11), (1, 13),
-- Bus 2: Standard (25 places: 8 premium + 17 standard, AC, no wifi)
(2, 2), (2, 5), (2, 8), (2, 10), (2, 13),
-- Bus 3: Confort Plus (20 places: 6 premium + 14 standard, AC, wifi)
(3, 3), (3, 6), (3, 9), (3, 10), (3, 12);

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

-- FEBRUARY 2026
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

-- Reservations WITHOUT ClassePlace (to be updated manually when testing)
INSERT INTO Reservation (numero_place, id_classePlace, id_client, id_bus_voyage) VALUES 
-- January reservations
(1, NULL, 1, 1),   -- Jan 15 - Economique local
(2, NULL, 2, 1),   -- Jan 15 - Economique local
(5, NULL, 3, 2),   -- Jan 16 - Standard local
(1, NULL, 4, 4),   -- Jan 20 - Toamasina Economique
(2, NULL, 5, 5),   -- Jan 22 - Toamasina Standard
(10, NULL, 6, 6),  -- Jan 25 - Toamasina Confort
(1, NULL, 7, 7),   -- Jan 28 - Antsirabe
(2, NULL, 8, 8),   -- Jan 30 - Fianarantsoa

-- February reservations
(1, NULL, 1, 9),   -- Feb 5
(2, NULL, 2, 9),   -- Feb 5
(5, NULL, 3, 10),  -- Feb 10
(1, NULL, 4, 11),  -- Feb 15
(2, NULL, 5, 12);  -- Feb 20 (special price)

-- ReservationStatut (All active for testing)
INSERT INTO ReservationStatut (date_annulation, id_reservation) VALUES 
(NULL, 1), (NULL, 2), (NULL, 3), (NULL, 4), (NULL, 5),
(NULL, 6), (NULL, 7), (NULL, 8), (NULL, 9), (NULL, 10),
(NULL, 11), (NULL, 12), (NULL, 13);

-- Paiements (1-3 days before trip)
INSERT INTO Paiement (date_paiement, montant_paye, id_caisse, id_reservation) VALUES 
-- January payments
('2026-01-13 08:00:00', 12000.00, 1, 1),
('2026-01-13 08:30:00', 12000.00, 2, 2),
('2026-01-14 09:00:00', 22000.00, 1, 3),
('2026-01-18 06:00:00', 35000.00, 1, 4),
('2026-01-20 07:00:00', 35000.00, 2, 5),
('2026-01-23 08:00:00', 35000.00, 3, 6),
('2026-01-26 09:00:00', 18000.00, 1, 7),
('2026-01-28 10:00:00', 65000.00, 2, 8),

-- February payments
('2026-02-03 08:00:00', 12000.00, 1, 9),
('2026-02-03 08:30:00', 12000.00, 1, 10),
('2026-02-08 09:00:00', 35000.00, 2, 11),
('2026-02-13 10:00:00', 18000.00, 3, 12),
('2026-02-18 11:00:00', 80000.00, 3, 13);

-- Price History (minimal)
INSERT INTO HistoriquePrixClasse (date_ecriture, prix_classe, id_busClasse) VALUES 
('2025-10-01 00:00:00', 11000.00, 1),
('2026-01-01 00:00:00', 12000.00, 1),
('2025-10-01 00:00:00', 21000.00, 2),
('2026-01-01 00:00:00', 22000.00, 2),
('2025-10-01 00:00:00', 34000.00, 3),
('2026-01-01 00:00:00', 35000.00, 3);

INSERT INTO HistoriquePrixVoyage (date_ecriture, prix_voyage, id_voyage) VALUES 
('2025-09-01 00:00:00', 33500.00, 3),
('2025-12-01 00:00:00', 35000.00, 3),
('2025-09-01 00:00:00', 17000.00, 4),
('2025-12-01 00:00:00', 18000.00, 4),
('2025-09-01 00:00:00', 63500.00, 5),
('2025-12-01 00:00:00', 65000.00, 5);

INSERT INTO HistoriquePrixSpecifique (date_ecriture, prix_specifique, id_bus_voyage) VALUES 
('2026-02-15 00:00:00', 80000.00, 12);

--? Alea week 2:
INSERT INTO Voyage (duree, prix_voyage, id_gare_1, id_gare_2) VALUES 
(8.0, 40000.00, 8, 7);

INSERT INTO Bus (immatriculation, id_busClasse) VALUES 
('nyeh', 2);

INSERT INTO BusConf (libelle, valeur) VALUES 
('capacite', '18'),
('nb_place_VIP', '2'),
('nb_place_premium', '6'),
('nb_place_standard', '10');

INSERT INTO Bus_BusConf (id_bus, id_busConf) VALUES 
(4, 15),  -- capacite: 18
(4, 16),  -- nb_place_VIP: 2
(4, 17),  -- nb_place_premium: 6
(4, 18);  -- nb_place_standard: 10  (Note: your SQL had nb_place_VIP again, I'm assuming you meant standard)

INSERT INTO Gare (libelle) VALUES 
('Toamasina'),
('Antananarivo');

INSERT INTO Bus_Voyage (heure_depart, date_depart, prix_specifique, id_bus, id_voyage) VALUES 
('07:00:00', '2026-01-25', NULL, 4, 6);