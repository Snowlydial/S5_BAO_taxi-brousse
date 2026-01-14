\c taxibroussedb;

-- Caisse (Payment methods)
INSERT INTO Caisse (libelle) VALUES 
('Espece'),
('Mobile Money (MVola)'),
('Mobile Money (Orange Money)'),
('Mobile Money (Airtel Money)'),
('Carte Bancaire'),
('Virement Bancaire'),
('Cheque');

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

-- Gare (Major bus stations across Madagascar)
INSERT INTO Gare (libelle) VALUES 
-- Antananarivo (Capital)
('Fasan''karana'),
('Ambodivona'),
('Ambolomadinika'),
('Ampasampito'),
('Anosibe'),
('Analakely'),
('67 Ha'),
('Ambohimanarina'),
('Mausolée'),
('Andravoahangy'),
-- Provincial cities
('Toamasina Gare Routiere'),
('Antsirabe Gare Routiere'),
('Fianarantsoa Gare Routiere'),
('Mahajanga Gare Routiere'),
('Toliara Gare Routiere'),
('Antsiranana (Diego-Suarez)'),
('Morondava Gare Routiere'),
('Nosy Be Gare Routiere'),
-- Other major towns
('Ambositra Centre'),
('Antsohihy Gare'),
('Manakara Gare'),
('Fort Dauphin (Taolagnaro)'),
('Sambava Gare'),
('Antalaha Centre');

-- BusClasse
INSERT INTO BusClasse (libelle, prix_classe) VALUES 
('Economique', 12000.00),
('Standard', 22000.00),
('Confort Plus', 35000.00),
('VIP Luxe', 55000.00),
('Super VIP', 75000.00);

-- BusConf (Configuration types)
INSERT INTO BusConf (libelle, valeur) VALUES 
('capacite', '27'),
('capacite', '25'),
('capacite', '23'),
('capacite', '20'),
('capacite', '15'),
('capacite', '10'),
('climatisation', 'oui'),
('climatisation', 'non'),
('wifi', 'oui'),
('wifi', 'non'),
('tv', 'oui'),
('tv', 'non'),
('usb_charging', 'oui'),
('usb_charging', 'non'),
('toilettes', 'oui'),
('toilettes', 'non');

-- Bus (Sample vehicles with Malagasy registration)
INSERT INTO Bus (immatriculation, id_busClasse) VALUES 
-- Economique
('1234 TAA', 1),
('2345 TAB', 1),
('3456 TAC', 1),
('4567 TAD', 1),
-- Standard
('5678 TAE', 2),
('6789 TAF', 2),
('7890 TAG', 2),
('8901 TAH', 2),
-- Confort Plus
('9012 TAI', 3),
('1357 TAJ', 3),
('2468 TAK', 3),
-- VIP Luxe
('3579 TAL', 4),
('4680 TAM', 4),
('5791 TAN', 4),
-- Super VIP
('6802 TAO', 5),
('7913 TAP', 5);

-- Bus_BusConf (Link buses to their configurations)
-- Economique buses (72 places, no AC, no wifi, no tv, no toilets)
INSERT INTO Bus_BusConf (id_bus, id_busConf) VALUES 
(1, 1), (1, 8), (1, 10), (1, 12), (1, 14), (1, 16),
(2, 1), (2, 8), (2, 10), (2, 12), (2, 14), (2, 16),
(3, 2), (3, 8), (3, 10), (3, 12), (3, 14), (3, 16),
(4, 2), (4, 8), (4, 10), (4, 12), (4, 14), (4, 16);

-- Standard buses (45 places, AC, no wifi, no tv, no toilets)
INSERT INTO Bus_BusConf (id_bus, id_busConf) VALUES 
(5, 3), (5, 7), (5, 10), (5, 12), (5, 14), (5, 16),
(6, 3), (6, 7), (6, 10), (6, 12), (6, 14), (6, 16),
(7, 3), (7, 7), (7, 10), (7, 12), (7, 14), (7, 16),
(8, 3), (8, 7), (8, 10), (8, 12), (8, 14), (8, 16);

-- Confort Plus buses (35 places, AC, wifi, TV, USB, no toilets)
INSERT INTO Bus_BusConf (id_bus, id_busConf) VALUES 
(9, 4), (9, 7), (9, 9), (9, 11), (9, 13), (9, 16),
(10, 4), (10, 7), (10, 9), (10, 11), (10, 13), (10, 16),
(11, 4), (11, 7), (11, 9), (11, 11), (11, 13), (11, 16);

-- VIP Luxe buses (28 places, AC, wifi, TV, USB, toilets)
INSERT INTO Bus_BusConf (id_bus, id_busConf) VALUES 
(12, 5), (12, 7), (12, 9), (12, 11), (12, 13), (12, 15),
(13, 5), (13, 7), (13, 9), (13, 11), (13, 13), (13, 15),
(14, 5), (14, 7), (14, 9), (14, 11), (14, 13), (14, 15);

-- Super VIP buses (20 places, all amenities)
INSERT INTO Bus_BusConf (id_bus, id_busConf) VALUES 
(15, 6), (15, 7), (15, 9), (15, 11), (15, 13), (15, 15),
(16, 6), (16, 7), (16, 9), (16, 11), (16, 13), (16, 15);

-- Voyage (Popular routes in Madagascar with realistic durations)
INSERT INTO Voyage (duree, prix_voyage, id_gare_1, id_gare_2) VALUES 
-- Antananarivo local/suburban routes
(0.5, NULL, 1, 3),   -- Fasan'karana -> Ambolomadinika
(0.75, NULL, 1, 6),  -- Fasan'karana -> Analakely
(1.0, NULL, 3, 7),   -- Ambolomadinika -> 67 Ha
(0.5, NULL, 6, 9),   -- Analakely -> Mausolee
(1.5, NULL, 1, 10),  -- Fasan'karana -> Andravoahangy

-- Major intercity routes (RN2 - East Coast)
(8.0, 35000.00, 1, 11),   -- Antananarivo -> Toamasina (370km)
(10.0, 42000.00, 11, 21), -- Toamasina -> Manakara (coast)

-- RN7 - South (Most popular tourist route)
(3.5, 18000.00, 1, 12),   -- Antananarivo -> Antsirabe (169km)
(4.0, 22000.00, 12, 19),  -- Antsirabe -> Ambositra (90km)
(3.5, 20000.00, 19, 13),  -- Ambositra -> Fianarantsoa (145km)
(12.0, 65000.00, 1, 13),  -- Antananarivo -> Fianarantsoa (direct)
(10.0, 58000.00, 13, 15), -- Fianarantsoa -> Toliara (coastal)

-- RN4 - West Coast
(12.0, 68000.00, 1, 14),  -- Antananarivo -> Mahajanga (570km)
(8.0, 45000.00, 14, 17),  -- Mahajanga -> Morondava

-- RN6 - North
(18.0, 95000.00, 1, 16),  -- Antananarivo -> Antsiranana (1100km)
(4.0, 25000.00, 16, 18),  -- Antsiranana -> Nosy Be (ferry connection)
(14.0, 75000.00, 1, 20),  -- Antananarivo -> Antsohihy (north)

-- East coast routes
(16.0, 85000.00, 1, 22),  -- Antananarivo -> Fort Dauphin (southeast)
(12.0, 70000.00, 11, 23), -- Toamasina -> Sambava (vanilla coast)
(3.0, 18000.00, 23, 24);  -- Sambava -> Antalaha (vanilla route)

-- Bus_Voyage (Scheduled trips - realistic Madagascar schedule)
-- January 14, 2026 (Today)
INSERT INTO Bus_Voyage (heure_depart, date_depart, prix_specifique, id_bus, id_voyage) VALUES 
-- Local Tana routes - frequent departures
('05:00:00', '2026-01-14', NULL, 1, 1),
('06:30:00', '2026-01-14', NULL, 2, 1),
('08:00:00', '2026-01-14', NULL, 3, 1),
('10:00:00', '2026-01-14', NULL, 1, 2),
('12:00:00', '2026-01-14', NULL, 2, 2),
('14:00:00', '2026-01-14', NULL, 3, 3),
('15:30:00', '2026-01-14', NULL, 4, 4),
('16:00:00', '2026-01-14', NULL, 1, 5),

-- RN2 to Toamasina (early morning departures)
('05:00:00', '2026-01-14', NULL, 5, 6),
('06:00:00', '2026-01-14', NULL, 6, 6),
('07:00:00', '2026-01-14', NULL, 9, 6),
('08:00:00', '2026-01-14', NULL, 12, 6),

-- RN7 routes (Antsirabe)
('06:00:00', '2026-01-14', NULL, 7, 8),
('08:00:00', '2026-01-14', NULL, 8, 8),
('10:00:00', '2026-01-14', NULL, 10, 8),
('14:00:00', '2026-01-14', NULL, 11, 8),

-- RN7 to Fianarantsoa (long haul - early departure)
('04:00:00', '2026-01-14', NULL, 9, 11),
('05:00:00', '2026-01-14', NULL, 13, 11),

-- West to Mahajanga (overnight preparation)
('04:00:00', '2026-01-14', NULL, 12, 13),
('05:30:00', '2026-01-14', NULL, 14, 13),

-- North to Antsiranana (very early - long journey)
('03:00:00', '2026-01-14', NULL, 15, 15),

-- January 15, 2026 (Tomorrow)
('05:00:00', '2026-01-15', NULL, 1, 1),
('06:30:00', '2026-01-15', NULL, 2, 1),
('05:00:00', '2026-01-15', NULL, 5, 6),
('06:00:00', '2026-01-15', NULL, 6, 6),
('07:00:00', '2026-01-15', NULL, 9, 6),
('06:00:00', '2026-01-15', NULL, 7, 8),
('08:00:00', '2026-01-15', NULL, 8, 8),
('04:00:00', '2026-01-15', NULL, 9, 11),
('04:00:00', '2026-01-15', NULL, 12, 13),

-- January 16, 2026
('05:00:00', '2026-01-16', NULL, 1, 1),
('10:00:00', '2026-01-16', NULL, 2, 2),
('05:00:00', '2026-01-16', NULL, 5, 6),
('06:00:00', '2026-01-16', NULL, 7, 8),
('04:00:00', '2026-01-16', NULL, 9, 11),

-- January 17, 2026
('06:30:00', '2026-01-17', NULL, 2, 1),
('08:00:00', '2026-01-17', NULL, 3, 1),
('06:00:00', '2026-01-17', NULL, 6, 6),
('08:00:00', '2026-01-17', NULL, 8, 8),
('05:30:00', '2026-01-17', NULL, 14, 13),

-- January 18, 2026
('05:00:00', '2026-01-18', NULL, 1, 1),
('05:00:00', '2026-01-18', NULL, 5, 6),
('06:00:00', '2026-01-18', NULL, 7, 8),
('04:00:00', '2026-01-18', NULL, 9, 11),
('04:00:00', '2026-01-18', NULL, 12, 13),

-- January 19-21, 2026 (Extended schedule)
('05:00:00', '2026-01-19', NULL, 1, 1),
('06:00:00', '2026-01-19', NULL, 5, 6),
('04:00:00', '2026-01-19', NULL, 9, 11),
('05:00:00', '2026-01-20', NULL, 2, 1),
('07:00:00', '2026-01-20', NULL, 9, 6),
('08:00:00', '2026-01-20', NULL, 10, 8),
('05:00:00', '2026-01-21', NULL, 1, 1),
('05:00:00', '2026-01-21', NULL, 5, 6),
('03:00:00', '2026-01-21', NULL, 15, 15);

-- Client (Malagasy names - diverse demographics)
INSERT INTO Client (nom, prenom, id_categorieGenre, id_categorieGroupeAge) VALUES 
-- Adult men
('Rakoto', 'Jean', 1, 3),
('Randria', 'Paul', 1, 3),
('Andrianina', 'Luc', 1, 3),
('Rabemananjara', 'David', 1, 3),
('Raharimanana', 'Michel', 1, 3),
('Rasolofo', 'Claude', 1, 3),
('Rabary', 'Herilanto', 1, 3),
('Randriamanana', 'Toky', 1, 3),
('Andriamahefa', 'Hery', 1, 3),
('Razafindrabe', 'Nirina', 1, 3),

-- Adult women
('Rasoa', 'Marie', 2, 3),
('Raharison', 'Sophie', 2, 3),
('Razafindrakoto', 'Clara', 2, 3),
('Rasoanaivo', 'Emma', 2, 3),
('Rakotomalala', 'Nicole', 2, 3),
('Rasolofonirina', 'Aina', 2, 3),
('Randriamahery', 'Hajasoa', 2, 3),
('Rafaralahy', 'Lalaina', 2, 3),
('Rakotondrasoa', 'Voahirana', 2, 3),
('Ranaivo', 'Tahiana', 2, 3),

-- Seniors
('Rakotondrabe', 'Georges', 1, 4),
('Raharimalala', 'Josephine', 2, 4),
('Andrianaivo', 'Albert', 1, 4),
('Razafimahefa', 'Celestine', 2, 4),

-- Teenagers
('Andriamaro', 'Toky', 1, 2),
('Rabetsitonta', 'Kanto', 1, 2),
('Rabenoro', 'Faly', 2, 2),
('Rakotoarisoa', 'Sitraka', 1, 2),

-- Children
('Rajaonah', 'Miora', 2, 1),
('Rabe', 'Tiana', 2, 1),
('Randriamalala', 'Lova', 1, 1),
('Razafy', 'Nomena', 1, 1);

-- Sample Reservations (realistic booking patterns)
INSERT INTO Reservation (numero_place, id_client, id_bus_voyage) VALUES 
-- Bus_Voyage 1 (Local 05:00) - morning commuters - 8 seats
(1, 1, 1),
(2, 2, 1),
(3, 3, 1),
(10, 4, 1),
(11, 5, 1),
(20, 6, 1),
(25, 7, 1),
(30, 8, 1),

-- Bus_Voyage 9 (Toamasina 05:00 Economique) - family trip - 12 seats
(1, 9, 9),
(2, 10, 9),
(3, 11, 9),
(4, 29, 9),
(5, 30, 9),
(15, 12, 9),
(16, 13, 9),
(20, 14, 9),
(21, 15, 9),
(30, 16, 9),
(31, 17, 9),
(32, 18, 9),

-- Bus_Voyage 10 (Toamasina 06:00 Standard) - business travelers - 6 seats
(1, 1, 10),
(2, 3, 10),
(10, 5, 10),
(11, 7, 10),
(15, 9, 10),
(20, 19, 10),

-- Bus_Voyage 11 (Toamasina 07:00 Confort Plus) - comfort seekers - 10 seats
(1, 2, 11),
(2, 4, 11),
(5, 6, 11),
(6, 8, 11),
(10, 10, 11),
(11, 12, 11),
(15, 14, 11),
(16, 16, 11),
(20, 18, 11),
(25, 20, 11),

-- Bus_Voyage 12 (Toamasina 08:00 VIP) - luxury travelers - 5 seats
(1, 21, 12),
(2, 22, 12),
(5, 1, 12),
(10, 3, 12),
(15, 5, 12),

-- Bus_Voyage 13 (Antsirabe 06:00) - mixed group - 7 seats
(1, 11, 13),
(2, 12, 13),
(10, 13, 13),
(11, 14, 13),
(20, 15, 13),
(25, 27, 13),
(30, 28, 13),

-- Bus_Voyage 17 (Fianarantsoa 04:00) - early birds - 9 seats
(1, 7, 17),
(2, 8, 17),
(3, 9, 17),
(5, 10, 17),
(10, 23, 17),
(11, 24, 17),
(15, 25, 17),
(20, 26, 17),
(25, 1, 17);

-- ReservationStatut (Initial active status for all reservations)
INSERT INTO ReservationStatut (date_annulation, id_reservation) VALUES 
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
(NULL, 61), (NULL, 62), (NULL, 63);

-- Add some cancelled reservations (realistic scenarios)
-- Customer cancelled 2 days before
INSERT INTO ReservationStatut (date_annulation, id_reservation) VALUES 
('2026-01-12', 4),
('2026-01-12', 5);

-- Same day cancellation
INSERT INTO ReservationStatut (date_annulation, id_reservation) VALUES 
('2026-01-14', 6);

-- Sample Paiements (various payment scenarios)
INSERT INTO Paiement (date_paiement, montant_paye, id_caisse, id_reservation) VALUES 
-- Local route payments (cheaper)
('2026-01-13 08:00:00', 12000.00, 1, 1), -- Espece
('2026-01-13 08:15:00', 12000.00, 2, 2), -- MVola
('2026-01-13 09:00:00', 12000.00, 3, 3), -- Orange Money
('2026-01-13 09:30:00', 12000.00, 1, 4), -- Cancelled later
('2026-01-13 09:45:00', 12000.00, 2, 5), -- Cancelled later
('2026-01-14 05:00:00', 12000.00, 1, 6), -- Same day cancellation
('2026-01-13 10:00:00', 12000.00, 2, 7),
('2026-01-13 11:00:00', 12000.00, 4, 8),

-- Toamasina Economique (Bus voyage 9) - family booking
('2026-01-12 14:00:00', 47000.00, 1, 9), -- Adult full price
('2026-01-12 14:00:00', 47000.00, 1, 10), -- Adult
('2026-01-12 14:00:00', 47000.00, 1, 11), -- Adult
('2026-01-12 14:00:00', 47000.00, 1, 12), -- Child
('2026-01-12 14:00:00', 47000.00, 1, 13), -- Child
('2026-01-12 14:30:00', 47000.00, 2, 14), -- Adult MVola
('2026-01-12 14:30:00', 47000.00, 2, 15), -- Adult
('2026-01-12 15:00:00', 47000.00, 3, 16), -- Orange Money
('2026-01-12 15:00:00', 47000.00, 3, 17),
('2026-01-12 15:30:00', 47000.00, 1, 18),
('2026-01-12 15:30:00', 47000.00, 1, 19),
('2026-01-12 16:00:00', 47000.00, 2, 20),

-- Toamasina Standard (Bus voyage 10) - business travelers, some split payments
('2026-01-11 10:00:00', 57000.00, 5, 21), -- Card payment
('2026-01-11 11:00:00', 57000.00, 5, 22), -- Card
('2026-01-11 14:00:00', 40000.00, 2, 23), -- Split payment part 1
('2026-01-11 14:00:00', 17000.00, 1, 23), -- Split payment part 2
('2026-01-12 09:00:00', 57000.00, 2, 24),
('2026-01-12 10:00:00', 57000.00, 3, 25),
('2026-01-12 11:00:00', 57000.00, 5, 26),

-- Toamasina Confort Plus (Bus voyage 11)
('2026-01-10 15:00:00', 70000.00, 5, 27), -- Card
('2026-01-10 15:30:00', 70000.00, 5, 28),
('2026-01-11 09:00:00', 70000.00, 2, 29), -- MVola
('2026-01-11 09:30:00', 70000.00, 2, 30),
('2026-01-11 10:00:00', 70000.00, 3, 31),
('2026-01-11 10:30:00', 70000.00, 3, 32),
('2026-01-11 11:00:00', 70000.00, 2, 33),
('2026-01-11 14:00:00', 70000.00, 5, 34),
('2026-01-11 14:30:00', 70000.00, 5, 35),
('2026-01-11 15:00:00', 70000.00, 2, 36),

-- Toamasina VIP (Bus voyage 12) - premium, multiple payment methods
('2026-01-09 10:00:00', 90000.00, 5, 37), -- Full card
('2026-01-09 11:00:00', 50000.00, 5, 38), -- Split card
('2026-01-09 11:00:00', 40000.00, 2, 38), -- Split MVola
('2026-01-10 09:00:00', 90000.00, 6, 39), -- Bank transfer
('2026-01-10 10:00:00', 90000.00, 5, 40),
('2026-01-10 14:00:00', 90000.00, 5, 41),

-- Antsirabe Standard (Bus voyage 13)
('2026-01-12 16:00:00', 40000.00, 1, 42),
('2026-01-12 16:15:00', 40000.00, 2, 43),
('2026-01-12 16:30:00', 40000.00, 2, 44),
('2026-01-12 17:00:00', 40000.00, 1, 45),
('2026-01-13 08:00:00', 40000.00, 3, 46),
('2026-01-13 09:00:00', 40000.00, 2, 47),
('2026-01-13 10:00:00', 40000.00, 1, 48),

-- Fianarantsoa Confort Plus (Bus voyage 17)
('2026-01-10 14:00:00', 100000.00, 5, 49),
('2026-01-10 14:30:00', 100000.00, 5, 50),
('2026-01-10 15:00:00', 100000.00, 2, 51),
('2026-01-11 09:00:00', 100000.00, 3, 52),
('2026-01-11 10:00:00', 100000.00, 2, 53),
('2026-01-11 11:00:00', 100000.00, 2, 54),
('2026-01-11 14:00:00', 60000.00, 2, 55), -- Split
('2026-01-11 14:00:00', 40000.00, 1, 55), -- Split
('2026-01-12 09:00:00', 100000.00, 5, 56),
('2026-01-12 10:00:00', 100000.00, 5, 57);

-- Add price history (realistic Madagascar inflation)
INSERT INTO HistoriquePrixClasse (date_ecriture, prix_classe, id_busClasse) VALUES 
('2025-07-01 00:00:00', 10000.00, 1),
('2025-10-01 00:00:00', 11000.00, 1),
('2026-01-01 00:00:00', 12000.00, 1),


('2025-07-01 00:00:00', 20000.00, 2),
('2025-10-01 00:00:00', 21000.00, 2),
('2026-01-01 00:00:00', 22000.00, 2),
('2025-07-01 00:00:00', 32000.00, 3),
('2025-10-01 00:00:00', 33500.00, 3),
('2026-01-01 00:00:00', 35000.00, 3),
('2025-07-01 00:00:00', 50000.00, 4),
('2025-10-01 00:00:00', 52500.00, 4),
('2026-01-01 00:00:00', 55000.00, 4),
('2025-07-01 00:00:00', 70000.00, 5),
('2025-10-01 00:00:00', 72500.00, 5),
('2026-01-01 00:00:00', 75000.00, 5);
INSERT INTO HistoriquePrixVoyage (date_ecriture, prix_voyage, id_voyage) VALUES
-- Toamasina route price evolution
('2025-06-01 00:00:00', 32000.00, 6),
('2025-09-01 00:00:00', 33500.00, 6),
('2025-12-01 00:00:00', 35000.00, 6),
-- Antsirabe route
('2025-06-01 00:00:00', 16000.00, 8),
('2025-09-01 00:00:00', 17000.00, 8),
('2025-12-01 00:00:00', 18000.00, 8),
-- Fianarantsoa route
('2025-06-01 00:00:00', 62000.00, 11),
('2025-09-01 00:00:00', 63500.00, 11),
('2025-12-01 00:00:00', 65000.00, 11),
-- Mahajanga route
('2025-06-01 00:00:00', 65000.00, 13),
('2025-09-01 00:00:00', 66500.00, 13),
('2025-12-01 00:00:00', 68000.00, 13),
-- Antsiranana route
('2025-06-01 00:00:00', 90000.00, 15),
('2025-09-01 00:00:00', 92500.00, 15),
('2025-12-01 00:00:00', 95000.00, 15);

INSERT INTO HistoriquePrixSpecifique (date_ecriture, prix_specifique, id_bus_voyage) VALUES
-- Holiday surcharge for some routes
('2026-01-10 00:00:00', 42000.00, 9),  -- Toamasina peak
('2026-01-10 00:00:00', 75000.00, 11), -- Confort Plus peak
('2026-01-10 00:00:00', 48000.00, 13); -- Antsirabe weekend