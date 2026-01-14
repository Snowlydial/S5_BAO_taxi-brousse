\c taxibroussedb;

-- Caisse (Payment methods)
INSERT INTO Caisse (libelle) VALUES 
('Espece'),
('Mobile Money (MVola)'),
('Mobile Money (Orange Money)'),
('Carte Bancaire'),
('Virement Bancaire');

-- CategorieGenre
INSERT INTO CategorieGenre (libelle) VALUES 
('Homme'),
('Femme');

-- CategorieGroupeAge
INSERT INTO CategorieGroupeAge (libelle) VALUES 
('Enfant (0-12 ans)'),
('Adolescent (13-17 ans)'),
('Adulte (18-59 ans)'),
('Senior (60+ ans)');

-- Gare (Major bus stations in Madagascar)
INSERT INTO Gare (libelle) VALUES 
-- Antananarivo area
('Fasan''karana'),
('Ambodivona'),
('Ambolomadinika'),
('Analakely'),
('67 Ha'),
-- Major cities
('Toamasina Gare Routiere'),
('Antsirabe Gare Routiere'),
('Fianarantsoa Gare Routiere'),
('Mahajanga Gare Routiere'),
('Toliara Gare Routiere');

-- BusClasse
INSERT INTO BusClasse (libelle, prix_classe) VALUES 
('Economique', 12000.00),
('Standard', 22000.00),
('Confort Plus', 35000.00),
('VIP Luxe', 55000.00);

-- BusConf (Configuration types)
INSERT INTO BusConf (libelle, valeur) VALUES 
('capacite', '30'),
('capacite', '25'),
('capacite', '20'),
('capacite', '15'),
('climatisation', 'oui'),
('climatisation', 'non'),
('wifi', 'oui'),
('wifi', 'non');

-- Bus (Sample vehicles)
INSERT INTO Bus (immatriculation, id_busClasse) VALUES 
('1234 TAA', 1), -- Economique
('2345 TAB', 1),
('3456 TAC', 2), -- Standard
('4567 TAD', 2),
('5678 TAE', 3), -- Confort Plus
('6789 TAF', 3),
('7890 TAG', 4), -- VIP Luxe
('8901 TAH', 4);

-- Bus_BusConf (Link buses to configurations)
INSERT INTO Bus_BusConf (id_bus, id_busConf) VALUES 
-- Economique (30 places, no AC, no wifi)
(1, 1), (1, 6), (1, 8),
(2, 1), (2, 6), (2, 8),
-- Standard (25 places, AC, no wifi)
(3, 2), (3, 5), (3, 8),
(4, 2), (4, 5), (4, 8),
-- Confort Plus (20 places, AC, wifi)
(5, 3), (5, 5), (5, 7),
(6, 3), (6, 5), (6, 7),
-- VIP Luxe (15 places, AC, wifi)
(7, 4), (7, 5), (7, 7),
(8, 4), (8, 5), (8, 7);

-- Voyage (Routes showing 3 price scenarios)
INSERT INTO Voyage (duree, prix_voyage, id_gare_1, id_gare_2) VALUES 
-- CASE 1: NULL prix_voyage (will use prix_classe)
(0.5, NULL, 1, 3),        -- Local Tana route
(1.0, NULL, 3, 5),        -- Another local route

-- CASE 2: Specific prix_voyage (overrides prix_classe)
(8.0, 35000.00, 1, 6),    -- Tana -> Toamasina
(3.5, 18000.00, 1, 7),    -- Tana -> Antsirabe
(12.0, 65000.00, 1, 8),   -- Tana -> Fianarantsoa

-- CASE 3: Will have prix_specifique in Bus_Voyage (highest priority)
(12.0, 68000.00, 1, 9),   -- Tana -> Mahajanga
(18.0, 95000.00, 1, 10);  -- Tana -> Toliara

-- Bus_Voyage (Showing all 3 price hierarchy cases)
INSERT INTO Bus_Voyage (heure_depart, date_depart, prix_specifique, id_bus, id_voyage) VALUES 
-- CASE 1: NULL prix_specifique, NULL prix_voyage -> uses prix_classe only
('06:00:00', '2026-01-14', NULL, 1, 1),  -- Will use 12000 (Economique)
('08:00:00', '2026-01-14', NULL, 3, 1),  -- Will use 22000 (Standard)
('10:00:00', '2026-01-14', NULL, 5, 2),  -- Will use 35000 (Confort Plus)

-- CASE 2: NULL prix_specifique, has prix_voyage -> uses prix_voyage
('05:00:00', '2026-01-14', NULL, 1, 3),  -- Will use 35000 (voyage price)
('06:00:00', '2026-01-14', NULL, 3, 3),  -- Will use 35000 (voyage price)
('07:00:00', '2026-01-14', NULL, 5, 3),  -- Will use 35000 (voyage price)
('06:00:00', '2026-01-14', NULL, 3, 4),  -- Will use 18000 (voyage price)
('08:00:00', '2026-01-14', NULL, 5, 4),  -- Will use 18000 (voyage price)
('04:00:00', '2026-01-14', NULL, 5, 5),  -- Will use 65000 (voyage price)
('05:00:00', '2026-01-14', NULL, 7, 5),  -- Will use 65000 (voyage price)

-- CASE 3: Has prix_specifique -> uses prix_specifique (highest priority)
('04:00:00', '2026-01-14', 75000.00, 7, 6),  -- Special price (normally 68000)
('05:30:00', '2026-01-14', 80000.00, 7, 6),  -- Peak hour surcharge
('03:00:00', '2026-01-14', 100000.00, 8, 7), -- Premium service (normally 95000)

-- Tomorrow
('06:00:00', '2026-01-15', NULL, 1, 1),
('05:00:00', '2026-01-15', NULL, 1, 3),
('06:00:00', '2026-01-15', NULL, 3, 4),
('04:00:00', '2026-01-15', NULL, 5, 5),

-- Next few days
('06:00:00', '2026-01-16', NULL, 1, 1),
('05:00:00', '2026-01-16', NULL, 1, 3),
('06:00:00', '2026-01-17', NULL, 3, 4),
('04:00:00', '2026-01-18', NULL, 5, 5);

-- Client (Malagasy names)
INSERT INTO Client (nom, prenom, id_categorieGenre, id_categorieGroupeAge) VALUES 
-- Adults
('Rakoto', 'Jean', 1, 3),
('Rasoa', 'Marie', 2, 3),
('Randria', 'Paul', 1, 3),
('Raharison', 'Sophie', 2, 3),
('Andrianina', 'Luc', 1, 3),
('Razafindrakoto', 'Clara', 2, 3),
('Rabemananjara', 'David', 1, 3),
('Rasoanaivo', 'Emma', 2, 3),
-- Seniors
('Raharimanana', 'Michel', 1, 4),
('Rakotomalala', 'Nicole', 2, 4),
-- Teenagers
('Andriamaro', 'Toky', 1, 2),
('Rasolofonirina', 'Aina', 2, 2),
-- Children
('Rabetsitonta', 'Kanto', 1, 1),
('Rajaonah', 'Faly', 2, 1);

-- Sample Reservations
INSERT INTO Reservation (numero_place, id_client, id_bus_voyage) VALUES 
-- Local routes
(1, 1, 1),
(2, 2, 1),
(5, 3, 1),
(1, 4, 2),
(2, 5, 2),

-- Toamasina routes (various classes)
(1, 6, 4),
(2, 7, 4),
(10, 8, 4),
(1, 1, 5),
(2, 2, 5),
(1, 3, 6),
(2, 4, 6),
(5, 5, 6),

-- Antsirabe
(1, 9, 7),
(2, 10, 7),
(10, 11, 8),

-- Fianarantsoa
(1, 12, 9),
(2, 13, 9),
(3, 14, 10);

-- ReservationStatut (Active status for all)
INSERT INTO ReservationStatut (date_annulation, id_reservation) VALUES 
(NULL, 1), (NULL, 2), (NULL, 3), (NULL, 4), (NULL, 5),
(NULL, 6), (NULL, 7), (NULL, 8), (NULL, 9), (NULL, 10),
(NULL, 11), (NULL, 12), (NULL, 13), (NULL, 14), (NULL, 15),
(NULL, 16), (NULL, 17), (NULL, 18), (NULL, 19);

-- Add 2 cancelled reservations
INSERT INTO ReservationStatut (date_annulation, id_reservation) VALUES 
('2026-01-12', 3),  -- Cancelled 2 days before
('2026-01-14', 5);  -- Same day cancellation

-- Sample Paiements
INSERT INTO Paiement (date_paiement, montant_paye, id_caisse, id_reservation) VALUES 
-- CASE 1 payments (prix_classe only)
('2026-01-13 08:00:00', 12000.00, 1, 1),  -- Economique
('2026-01-13 08:30:00', 12000.00, 2, 2),  -- Economique
('2026-01-13 09:00:00', 12000.00, 1, 3),  -- Cancelled
('2026-01-13 10:00:00', 22000.00, 2, 4),  -- Standard
('2026-01-14 06:00:00', 22000.00, 1, 5),  -- Standard, same day cancel

-- CASE 2 payments (prix_voyage)
('2026-01-12 14:00:00', 35000.00, 1, 6),  -- Toamasina
('2026-01-12 14:30:00', 35000.00, 2, 7),
('2026-01-12 15:00:00', 35000.00, 5, 8),
('2026-01-11 10:00:00', 35000.00, 4, 9),
('2026-01-11 11:00:00', 35000.00, 2, 10),
('2026-01-10 15:00:00', 35000.00, 4, 11),
('2026-01-10 15:30:00', 35000.00, 4, 12),
('2026-01-10 16:00:00', 35000.00, 2, 13),

-- CASE 3 payments (prix_specifique - highest priority)
('2026-01-09 10:00:00', 75000.00, 4, 14),  -- Special price
('2026-01-09 11:00:00', 80000.00, 5, 15),  -- Peak surcharge
('2026-01-08 14:00:00', 100000.00, 5, 16), -- Premium
('2026-01-08 15:00:00', 100000.00, 4, 17),
('2026-01-08 16:00:00', 65000.00, 3, 18),
('2026-01-08 17:00:00', 65000.00, 3, 19);

-- Price history
INSERT INTO HistoriquePrixClasse (date_ecriture, prix_classe, id_busClasse) VALUES 
('2025-07-01 00:00:00', 10000.00, 1),
('2025-10-01 00:00:00', 11000.00, 1),
('2026-01-01 00:00:00', 12000.00, 1);

INSERT INTO HistoriquePrixVoyage (date_ecriture, prix_voyage, id_voyage) VALUES 
('2025-06-01 00:00:00', 32000.00, 3),
('2025-09-01 00:00:00', 33500.00, 3),
('2025-12-01 00:00:00', 35000.00, 3);

INSERT INTO HistoriquePrixSpecifique (date_ecriture, prix_specifique, id_bus_voyage) VALUES 
('2026-01-10 00:00:00', 75000.00, 14),  -- Special pricing set
('2026-01-10 00:00:00', 80000.00, 15),  -- Peak hour pricing
('2026-01-10 00:00:00', 100000.00, 16); -- Premium service pricing