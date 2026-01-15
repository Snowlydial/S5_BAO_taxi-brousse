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

-- Bus_Voyage (Spread across 10 months: Sep 2025 to Jun 2026)
INSERT INTO Bus_Voyage (heure_depart, date_depart, prix_specifique, id_bus, id_voyage) VALUES 
-- SEPTEMBER 2025 (Start of school year, busy period)
('06:00:00', '2025-09-15', NULL, 1, 1),   -- Local route
('08:00:00', '2025-09-16', NULL, 3, 1),   -- Local route
('10:00:00', '2025-09-18', NULL, 5, 2),   -- Local route
('05:00:00', '2025-09-20', NULL, 1, 3),   -- Toamasina
('06:00:00', '2025-09-22', NULL, 3, 3),   -- Toamasina
('07:00:00', '2025-09-25', NULL, 5, 3),   -- Toamasina
('06:00:00', '2025-09-28', NULL, 3, 4),   -- Antsirabe
('04:00:00', '2025-09-30', NULL, 5, 5),   -- Fianarantsoa

-- OCTOBER 2025 (Continuing busy period)
('08:00:00', '2025-10-05', NULL, 1, 1),
('05:00:00', '2025-10-08', NULL, 1, 3),
('06:00:00', '2025-10-12', NULL, 3, 4),
('04:00:00', '2025-10-15', NULL, 5, 5),
('04:00:00', '2025-10-18', 75000.00, 7, 6),  -- Special price
('05:30:00', '2025-10-20', 80000.00, 7, 6),  -- Peak hour surcharge
('03:00:00', '2025-10-25', 100000.00, 8, 7), -- Premium service

-- NOVEMBER 2025
('06:00:00', '2025-11-02', NULL, 1, 1),
('05:00:00', '2025-11-05', NULL, 1, 3),
('06:00:00', '2025-11-10', NULL, 3, 4),
('04:00:00', '2025-11-15', NULL, 5, 5),

-- DECEMBER 2025 (Holiday season begins)
('06:00:00', '2025-12-01', NULL, 1, 1),
('05:00:00', '2025-12-05', NULL, 1, 3),
('06:00:00', '2025-12-10', NULL, 3, 4),
('04:00:00', '2025-12-15', NULL, 5, 5),
('04:00:00', '2025-12-20', 75000.00, 7, 6),  -- Holiday premium
('05:30:00', '2025-12-22', 85000.00, 7, 6),  -- Peak holiday price
('03:00:00', '2025-12-28', 110000.00, 8, 7), -- Premium holiday

-- JANUARY 2026 (New Year, very busy)
('06:00:00', '2026-01-02', NULL, 1, 1),
('05:00:00', '2026-01-04', NULL, 1, 3),
('06:00:00', '2026-01-06', NULL, 3, 4),
('04:00:00', '2026-01-08', NULL, 5, 5),
('04:00:00', '2026-01-10', 75000.00, 7, 6),
('05:30:00', '2026-01-12', 80000.00, 7, 6),
('03:00:00', '2026-01-15', 100000.00, 8, 7),

-- FEBRUARY 2026
('06:00:00', '2026-02-01', NULL, 1, 1),
('05:00:00', '2026-02-05', NULL, 1, 3),
('06:00:00', '2026-02-10', NULL, 3, 4),
('04:00:00', '2026-02-14', NULL, 5, 5),

-- MARCH 2026
('06:00:00', '2026-03-01', NULL, 1, 1),
('05:00:00', '2026-03-05', NULL, 1, 3),
('06:00:00', '2026-03-10', NULL, 3, 4),
('04:00:00', '2026-03-15', NULL, 5, 5),

-- APRIL 2026
('06:00:00', '2026-04-01', NULL, 1, 1),
('05:00:00', '2026-04-05', NULL, 1, 3),
('06:00:00', '2026-04-10', NULL, 3, 4),
('04:00:00', '2026-04-15', NULL, 5, 5),

-- MAY 2026
('06:00:00', '2026-05-01', NULL, 1, 1),   -- Labor Day
('05:00:00', '2026-05-05', NULL, 1, 3),
('06:00:00', '2026-05-10', NULL, 3, 4),
('04:00:00', '2026-05-15', NULL, 5, 5),

-- JUNE 2026 (End of school year, busy)
('06:00:00', '2026-06-01', NULL, 1, 1),
('05:00:00', '2026-06-05', NULL, 1, 3),
('06:00:00', '2026-06-10', NULL, 3, 4),
('04:00:00', '2026-06-15', NULL, 5, 5),
('04:00:00', '2026-06-20', 75000.00, 7, 6),
('05:30:00', '2026-06-25', 80000.00, 7, 6),
('03:00:00', '2026-06-28', 100000.00, 8, 7);

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

-- Sample Reservations (Spread across different Bus_Voyage dates)
-- Note: We need to match each reservation to a Bus_Voyage based on the ID sequence
INSERT INTO Reservation (numero_place, id_client, id_bus_voyage) VALUES 
-- September 2025 reservations
(1, 1, 1),   -- Sep 15
(2, 2, 1),   -- Sep 15
(5, 3, 2),   -- Sep 16
(1, 4, 3),   -- Sep 18
(2, 5, 4),   -- Sep 20

-- October 2025
(1, 6, 8),   -- Oct 5
(2, 7, 8),   -- Oct 5
(10, 8, 9),  -- Oct 8
(1, 1, 10),  -- Oct 12
(2, 2, 11),  -- Oct 15
(1, 3, 12),  -- Oct 18 (special price)
(2, 4, 13),  -- Oct 20 (peak surcharge)
(5, 5, 14),  -- Oct 25 (premium)

-- November 2025
(1, 9, 15),  -- Nov 2
(2, 10, 15), -- Nov 2
(10, 11, 16),-- Nov 5
(1, 12, 17), -- Nov 10
(2, 13, 18), -- Nov 15

-- December 2025
(1, 14, 19), -- Dec 1
(2, 1, 19),  -- Dec 1
(3, 2, 20),  -- Dec 5
(4, 3, 21),  -- Dec 10
(5, 4, 22),  -- Dec 15
(1, 5, 23),  -- Dec 20 (holiday premium)
(2, 6, 24),  -- Dec 22 (peak holiday)
(3, 7, 25),  -- Dec 28 (premium holiday)

-- January 2026
(1, 8, 26),  -- Jan 2
(2, 9, 26),  -- Jan 2
(5, 10, 27), -- Jan 4
(1, 11, 28), -- Jan 6
(2, 12, 29), -- Jan 8
(1, 13, 30), -- Jan 10
(2, 14, 31), -- Jan 12
(3, 1, 32),  -- Jan 15

-- February 2026
(1, 2, 33),  -- Feb 1
(2, 3, 33),  -- Feb 1
(5, 4, 34),  -- Feb 5
(1, 5, 35),  -- Feb 10
(2, 6, 36),  -- Feb 14

-- March 2026
(1, 7, 37),  -- Mar 1
(2, 8, 37),  -- Mar 1
(10, 9, 38), -- Mar 5
(1, 10, 39), -- Mar 10
(2, 11, 40), -- Mar 15

-- April 2026
(1, 12, 41), -- Apr 1
(2, 13, 41), -- Apr 1
(5, 14, 42), -- Apr 5
(1, 1, 43),  -- Apr 10
(2, 2, 44),  -- Apr 15

-- May 2026
(1, 3, 45),  -- May 1 (Labor Day)
(2, 4, 45),  -- May 1
(10, 5, 46), -- May 5
(1, 6, 47),  -- May 10
(2, 7, 48),  -- May 15

-- June 2026
(1, 8, 49),  -- Jun 1
(2, 9, 49),  -- Jun 1
(5, 10, 50), -- Jun 5
(1, 11, 51), -- Jun 10
(2, 12, 52), -- Jun 15
(1, 13, 53), -- Jun 20 (special)
(2, 14, 54), -- Jun 25 (peak)
(3, 1, 55);  -- Jun 28 (premium)

-- ReservationStatut (Active status for most, some cancelled)
INSERT INTO ReservationStatut (date_annulation, id_reservation) VALUES 
-- Active reservations (no cancellation date)
(NULL, 1), (NULL, 2), (NULL, 3), (NULL, 4), (NULL, 5),
(NULL, 6), (NULL, 7), (NULL, 8), (NULL, 9), (NULL, 10),
(NULL, 11), (NULL, 12), (NULL, 13), (NULL, 14), (NULL, 15),
(NULL, 16), (NULL, 17), (NULL, 18), (NULL, 19), (NULL, 20),
(NULL, 21), (NULL, 22), (NULL, 23), (NULL, 24), (NULL, 25),
(NULL, 26), (NULL, 27), (NULL, 28), (NULL, 29), (NULL, 30),
(NULL, 31), (NULL, 32), (NULL, 33), (NULL, 34), (NULL, 35),
(NULL, 36), (NULL, 37), (NULL, 38), (NULL, 39), (NULL, 40),
(NULL, 41), (NULL, 42), (NULL, 43), (NULL, 44), (NULL, 45),
(NULL, 46), (NULL, 47), (NULL, 48), (NULL, 49), (NULL, 50),
(NULL, 51), (NULL, 52), (NULL, 53), (NULL, 54), (NULL, 55),
(NULL, 56), (NULL, 57), (NULL, 58), (NULL, 59), (NULL, 60),
(NULL, 61), (NULL, 62),

-- Cancelled reservations (spread across months)
-- September cancellations
('2025-09-14', 3),  -- Cancelled 1 day before Sep 15 trip
-- October cancellations  
('2025-10-11', 10), -- Cancelled 1 day before Oct 12 trip
-- December cancellations (holiday change of plans)
('2025-12-09', 23), -- Cancelled 1 day before Dec 20 holiday trip
-- January cancellations
('2025-12-27', 28), -- New Year's Day cancellation
-- March cancellations
('2026-01-31', 37), -- Cancelled 1 day before Mar 1 trip
-- June cancellations
('2026-05-09', 54); -- Cancelled 1 day before Jun 25 peak trip

-- Sample Paiements (Spread across different months, typically 1-7 days before trip)
INSERT INTO Paiement (date_paiement, montant_paye, id_caisse, id_reservation) VALUES 
-- SEPTEMBER 2025 payments
('2025-09-13 08:00:00', 12000.00, 1, 1),   -- Sep 15 trip (Economique)
('2025-09-13 08:30:00', 12000.00, 2, 2),   -- Sep 15 trip (Mobile Money)
('2025-09-14 09:00:00', 12000.00, 1, 3),   -- Sep 16 trip (cancelled)
('2025-09-16 10:00:00', 22000.00, 2, 4),   -- Sep 18 trip (Standard)
('2025-09-18 06:00:00', 22000.00, 1, 5),   -- Sep 20 trip (Standard)

-- OCTOBER 2025 payments
('2025-09-03 14:00:00', 12000.00, 1, 6),   -- Oct 5 trip (Economique)
('2025-09-04 14:30:00', 12000.00, 2, 7),   -- Oct 5 trip
('2025-10-04 15:00:00', 35000.00, 5, 8),   -- Oct 8 trip (Toamasina)
('2025-10-07 10:00:00', 18000.00, 4, 9),   -- Oct 12 trip (Antsirabe)
('2025-10-10 11:00:00', 65000.00, 2, 10),  -- Oct 15 trip (Fianarantsoa)
('2025-10-15 15:00:00', 75000.00, 4, 11),  -- Oct 18 trip (Special price)
('2025-10-16 15:30:00', 80000.00, 4, 12),  -- Oct 20 trip (Peak surcharge)
('2025-10-19 16:00:00', 100000.00, 2, 13), -- Oct 25 trip (Premium)

-- NOVEMBER 2025 payments
('2025-10-22 10:00:00', 12000.00, 1, 14),  -- Nov 2 trip
('2025-10-24 11:00:00', 12000.00, 1, 15),  -- Nov 2 trip
('2025-11-01 15:00:00', 35000.00, 3, 16),  -- Nov 5 trip
('2025-11-03 14:00:00', 18000.00, 3, 17),  -- Nov 10 trip
('2025-11-08 15:00:00', 65000.00, 3, 18),  -- Nov 15 trip

-- DECEMBER 2025 payments (holiday season)
('2025-11-11 09:00:00', 12000.00, 1, 19),  -- Dec 1 trip
('2025-11-13 10:00:00', 12000.00, 1, 20),  -- Dec 1 trip
('2025-11-30 11:00:00', 35000.00, 2, 21),  -- Dec 5 trip
('2025-12-03 12:00:00', 18000.00, 2, 22),  -- Dec 10 trip
('2025-12-08 13:00:00', 65000.00, 4, 23),  -- Dec 15 trip
('2025-12-11 14:00:00', 75000.00, 4, 24),  -- Dec 20 trip (holiday premium)
('2025-12-18 15:00:00', 85000.00, 5, 25),  -- Dec 22 trip (peak holiday)
('2025-12-21 16:00:00', 110000.00, 5, 26), -- Dec 28 trip (premium holiday)

-- JANUARY 2026 payments (New Year)
('2025-12-23 08:00:00', 12000.00, 1, 27),  -- Jan 2 trip
('2025-12-23 09:00:00', 12000.00, 1, 28),  -- Jan 2 trip
('2026-01-01 10:00:00', 35000.00, 2, 29),  -- Jan 4 trip
('2026-01-03 11:00:00', 18000.00, 2, 30),  -- Jan 6 trip (cancelled)
('2026-01-05 12:00:00', 65000.00, 3, 31),  -- Jan 8 trip
('2026-01-07 13:00:00', 75000.00, 4, 32),  -- Jan 10 trip
('2026-01-09 14:00:00', 80000.00, 5, 33),  -- Jan 12 trip
('2026-01-11 15:00:00', 100000.00, 4, 34), -- Jan 15 trip

-- FEBRUARY 2026 payments
('2026-01-12 09:00:00', 12000.00, 1, 35),  -- Feb 1 trip
('2026-01-13 10:00:00', 12000.00, 1, 36),  -- Feb 1 trip
('2026-01-29 11:00:00', 35000.00, 2, 37),  -- Feb 5 trip
('2026-02-03 12:00:00', 18000.00, 2, 38),  -- Feb 10 trip
('2026-02-09 13:00:00', 65000.00, 3, 39),  -- Feb 14 trip

-- MARCH 2026 payments
('2026-02-12 09:00:00', 12000.00, 1, 40),  -- Mar 1 trip (cancelled)
('2026-02-12 10:00:00', 12000.00, 1, 41),  -- Mar 1 trip
('2026-02-27 11:00:00', 35000.00, 2, 42),  -- Mar 5 trip
('2026-03-05 12:00:00', 18000.00, 2, 43),  -- Mar 10 trip
('2026-03-09 13:00:00', 65000.00, 3, 44),  -- Mar 15 trip

-- APRIL 2026 payments
('2026-03-10 09:00:00', 12000.00, 1, 45),  -- Apr 1 trip
('2026-03-11 10:00:00', 12000.00, 1, 46),  -- Apr 1 trip
('2026-03-28 11:00:00', 35000.00, 2, 47),  -- Apr 5 trip
('2026-04-04 12:00:00', 18000.00, 2, 48),  -- Apr 10 trip
('2026-04-08 13:00:00', 65000.00, 3, 49),  -- Apr 15 trip

-- MAY 2026 payments
('2026-04-11 09:00:00', 12000.00, 1, 50),  -- May 1 trip (Labor Day)
('2026-04-11 10:00:00', 12000.00, 1, 51),  -- May 1 trip
('2026-04-22 11:00:00', 35000.00, 2, 52),  -- May 5 trip
('2026-05-03 12:00:00', 18000.00, 2, 53),  -- May 10 trip
('2026-05-07 13:00:00', 65000.00, 3, 54),  -- May 15 trip

-- JUNE 2026 payments (end of school year)
('2026-05-13 09:00:00', 12000.00, 1, 55),  -- Jun 1 trip
('2026-05-14 10:00:00', 12000.00, 1, 56),  -- Jun 1 trip
('2026-05-26 11:00:00', 35000.00, 2, 57),  -- Jun 5 trip
('2026-06-01 12:00:00', 18000.00, 2, 58),  -- Jun 10 trip
('2026-06-07 13:00:00', 65000.00, 3, 59),  -- Jun 15 trip
('2026-06-13 14:00:00', 75000.00, 4, 60),  -- Jun 20 trip (special)
('2026-06-19 15:00:00', 80000.00, 5, 61),  -- Jun 25 trip (peak - cancelled)
('2026-06-23 16:00:00', 100000.00, 4, 62); -- Jun 28 trip (premium)

-- Price history (showing price increases over time)
INSERT INTO HistoriquePrixClasse (date_ecriture, prix_classe, id_busClasse) VALUES 
-- Economique class price history
('2025-07-01 00:00:00', 10000.00, 1),
('2025-10-01 00:00:00', 11000.00, 1),
('2026-01-01 00:00:00', 12000.00, 1),
-- Standard class
('2025-07-01 00:00:00', 20000.00, 2),
('2025-10-01 00:00:00', 21000.00, 2),
('2026-01-01 00:00:00', 22000.00, 2),
-- Confort Plus
('2025-07-01 00:00:00', 33000.00, 3),
('2025-10-01 00:00:00', 34000.00, 3),
('2026-01-01 00:00:00', 35000.00, 3),
-- VIP Luxe
('2025-07-01 00:00:00', 52000.00, 4),
('2025-10-01 00:00:00', 53500.00, 4),
('2026-01-01 00:00:00', 55000.00, 4);

INSERT INTO HistoriquePrixVoyage (date_ecriture, prix_voyage, id_voyage) VALUES 
-- Tana -> Toamasina price history
('2025-06-01 00:00:00', 32000.00, 3),
('2025-09-01 00:00:00', 33500.00, 3),
('2025-12-01 00:00:00', 35000.00, 3),
-- Tana -> Antsirabe
('2025-06-01 00:00:00', 16000.00, 4),
('2025-09-01 00:00:00', 17000.00, 4),
('2025-12-01 00:00:00', 18000.00, 4),
-- Tana -> Fianarantsoa
('2025-06-01 00:00:00', 62000.00, 5),
('2025-09-01 00:00:00', 63500.00, 5),
('2025-12-01 00:00:00', 65000.00, 5);

INSERT INTO HistoriquePrixSpecifique (date_ecriture, prix_specifique, id_bus_voyage) VALUES 
-- Special pricing for holiday season
('2025-10-10 00:00:00', 75000.00, 12),  -- October special
('2025-12-15 00:00:00', 75000.00, 23),  -- December holiday premium
('2026-01-05 00:00:00', 75000.00, 30),  -- January special
-- Peak hour pricing
('2025-10-15 00:00:00', 80000.00, 13),  -- October peak
('2025-12-18 00:00:00', 85000.00, 24),  -- December peak holiday
('2026-01-08 00:00:00', 80000.00, 31),  -- January peak
-- Premium service pricing
('2025-10-20 00:00:00', 100000.00, 14),  -- October premium
('2025-12-24 00:00:00', 110000.00, 25),  -- December premium holiday
('2026-01-12 00:00:00', 100000.00, 32);  -- January premium