\c taxibroussedb;
-- Caisse (Payment methods)
INSERT INTO Caisse (libelle) VALUES 
('Espèce'),
('Mobile Money (MVola)'),
('Mobile Money (Orange Money)'),
('Carte Bancaire'),
('Virement Bancaire');

-- CategorieGenre
INSERT INTO CategorieGenre (libelle) VALUES 
('Homme'),
('Femme'),
('Autre');

-- CategorieGroupeAge
INSERT INTO CategorieGroupeAge (libelle) VALUES 
('Enfant (0-12 ans)'),
('Adolescent (13-17 ans)'),
('Adulte (18-59 ans)'),
('Senior (60+ ans)');

-- Gare (Major bus stations in Madagascar)
INSERT INTO Gare (libelle) VALUES 
('Fasan''kàrana'),
('Ambodivona'),
('Ambolomadinika'),
('Ampasampito'),
('Anosibe'),
('Analakely'),
('67 Ha'),
('Ambohimanarina'),
('Toamasina Gare Routière'),
('Antsirabe Gare Routière'),
('Fianarantsoa Gare Routière'),
('Mahajanga Gare Routière'),
('Toliara Gare Routière'),
('Antsiranana Gare Routière');

-- BusClasse
INSERT INTO BusClasse (libelle, prix_classe) VALUES 
('Economique', 15000.00),
('Standard', 25000.00),
('Confort', 35000.00),
('VIP', 50000.00);

-- BusConf (Configuration types)
INSERT INTO BusConf (libelle, valeur) VALUES 
('capacite', '67'),
('capacite', '45'),
('capacite', '30'),
('capacite', '25'),
('climatisation', 'oui'),
('climatisation', 'non'),
('wifi', 'oui'),
('wifi', 'non');

-- Bus (Sample vehicles)
INSERT INTO Bus (immatriculation, id_busClasse) VALUES 
('1234 TAA', 1), -- Economique
('2345 TAB', 1), -- Economique
('3456 TAC', 2), -- Standard
('4567 TAD', 2), -- Standard
('5678 TAE', 3), -- Confort
('6789 TAF', 3), -- Confort
('7890 TAG', 4), -- VIP
('8901 TAH', 4); -- VIP

-- Bus_BusConf (Link buses to their configurations)
-- Bus 1 (Economique, 67 places, no AC, no wifi)
INSERT INTO Bus_BusConf (id_bus, id_busConf) VALUES (1, 1), (1, 6), (1, 8);
-- Bus 2 (Economique, 67 places, no AC, no wifi)
INSERT INTO Bus_BusConf (id_bus, id_busConf) VALUES (2, 1), (2, 6), (2, 8);
-- Bus 3 (Standard, 45 places, AC, no wifi)
INSERT INTO Bus_BusConf (id_bus, id_busConf) VALUES (3, 2), (3, 5), (3, 8);
-- Bus 4 (Standard, 45 places, AC, no wifi)
INSERT INTO Bus_BusConf (id_bus, id_busConf) VALUES (4, 2), (4, 5), (4, 8);
-- Bus 5 (Confort, 30 places, AC, wifi)
INSERT INTO Bus_BusConf (id_bus, id_busConf) VALUES (5, 3), (5, 5), (5, 7);
-- Bus 6 (Confort, 30 places, AC, wifi)
INSERT INTO Bus_BusConf (id_bus, id_busConf) VALUES (6, 3), (6, 5), (6, 7);
-- Bus 7 (VIP, 25 places, AC, wifi)
INSERT INTO Bus_BusConf (id_bus, id_busConf) VALUES (7, 4), (7, 5), (7, 7);
-- Bus 8 (VIP, 25 places, AC, wifi)
INSERT INTO Bus_BusConf (id_bus, id_busConf) VALUES (8, 4), (8, 5), (8, 7);

-- Voyage (Popular routes in Madagascar)
INSERT INTO Voyage (duree, prix_voyage, id_gare_1, id_gare_2) VALUES 
-- Antananarivo local routes
(0.5, NULL, 1, 3),   -- Fasan'kàrana → Ambolomadinika
(0.75, NULL, 1, 6),  -- Fasan'kàrana → Analakely
(1.0, NULL, 3, 7),   -- Ambolomadinika → 67 Ha
-- Long distance routes
(8.0, 30000.00, 1, 9),   -- Antananarivo → Toamasina
(3.5, 20000.00, 1, 10),  -- Antananarivo → Antsirabe
(10.0, 45000.00, 1, 11), -- Antananarivo → Fianarantsoa
(12.0, 60000.00, 1, 12), -- Antananarivo → Mahajanga
(18.0, 80000.00, 1, 13), -- Antananarivo → Toliara
(22.0, 95000.00, 1, 14); -- Antananarivo → Antsiranana

-- Bus_Voyage (Scheduled trips for today and upcoming days)
-- Today's trips (January 14, 2026)
INSERT INTO Bus_Voyage (heure_depart, date_depart, prix_specifique, id_bus, id_voyage) VALUES 
-- Local routes - multiple departures
('06:00:00', '2026-01-14', NULL, 1, 1),
('08:00:00', '2026-01-14', NULL, 2, 1),
('10:00:00', '2026-01-14', NULL, 1, 2),
('14:00:00', '2026-01-14', NULL, 2, 2),
('15:00:00', '2026-01-14', NULL, 3, 3),
-- Long distance - morning departures
('06:00:00', '2026-01-14', NULL, 3, 4),  -- To Toamasina
('06:30:00', '2026-01-14', NULL, 4, 4),  -- To Toamasina
('07:00:00', '2026-01-14', NULL, 5, 5),  -- To Antsirabe
('05:00:00', '2026-01-14', NULL, 6, 6),  -- To Fianarantsoa
('06:00:00', '2026-01-14', NULL, 7, 7),  -- To Mahajanga
('04:00:00', '2026-01-14', NULL, 8, 8),  -- To Toliara

-- Tomorrow's trips (January 15, 2026)
('06:00:00', '2026-01-15', NULL, 1, 1),
('08:00:00', '2026-01-15', NULL, 2, 1),
('06:00:00', '2026-01-15', NULL, 3, 4),
('07:00:00', '2026-01-15', NULL, 5, 5),
('05:00:00', '2026-01-15', NULL, 6, 6),

-- Next week trips
('06:00:00', '2026-01-16', NULL, 1, 1),
('06:00:00', '2026-01-16', NULL, 3, 4),
('06:00:00', '2026-01-17', NULL, 2, 1),
('07:00:00', '2026-01-17', NULL, 5, 5),
('06:00:00', '2026-01-18', NULL, 1, 1),
('05:00:00', '2026-01-18', NULL, 6, 6);

-- Client (Sample customers)
INSERT INTO Client (nom, prenom, id_categorieGenre, id_categorieGroupeAge) VALUES 
('Rakoto', 'Jean', 1, 3),
('Rasoa', 'Marie', 2, 3),
('Randria', 'Paul', 1, 3),
('Raharison', 'Sophie', 2, 3),
('Andrianina', 'Luc', 1, 3),
('Razafindrakoto', 'Clara', 2, 3),
('Rabemananjara', 'David', 1, 3),
('Rasoanaivo', 'Emma', 2, 3),
('Raharimanana', 'Michel', 1, 4),
('Rakotomalala', 'Nicole', 2, 4),
('Andriamaro', 'Toky', 1, 2),
('Rasolofonirina', 'Aina', 2, 2),
('Rabetsitonta', 'Kanto', 1, 1),
('Rajaonah', 'Faly', 2, 1);

-- Sample Reservations (a few bookings already made)
INSERT INTO Reservation (numero_place, id_client, id_bus_voyage) VALUES 
-- Bus_Voyage 1 (06:00 local route) - 3 seats taken
(1, 1, 1),
(2, 2, 1),
(15, 3, 1),
-- Bus_Voyage 6 (To Toamasina 06:00) - 5 seats taken
(1, 4, 6),
(2, 4, 6),
(10, 5, 6),
(11, 6, 6),
(20, 7, 6);

-- Sample Paiements (payments for the reservations)
INSERT INTO Paiement (date_paiement, montant_paye, id_caisse, id_reservation) VALUES 
-- Single payments
(CURRENT_TIMESTAMP, 15000.00, 1, 1), -- Espèce
(CURRENT_TIMESTAMP, 15000.00, 2, 2), -- MVola
(CURRENT_TIMESTAMP, 15000.00, 1, 3), -- Espèce
-- Split payment example for seat 1 & 2 of bus voyage 6
(CURRENT_TIMESTAMP, 20000.00, 2, 4), -- MVola (partial)
(CURRENT_TIMESTAMP, 10000.00, 1, 4), -- Espèce (rest)
(CURRENT_TIMESTAMP, 30000.00, 4, 5), -- Carte
-- Full payments
(CURRENT_TIMESTAMP, 25000.00, 2, 6), -- MVola
(CURRENT_TIMESTAMP, 25000.00, 3, 7), -- Orange Money
(CURRENT_TIMESTAMP, 25000.00, 4, 8); -- Carte

-- Add some price history
INSERT INTO HistoriquePrixClasse (date_ecriture, prix_classe, id_busClasse) VALUES 
(CURRENT_TIMESTAMP - INTERVAL '30 days', 14000.00, 1),
(CURRENT_TIMESTAMP - INTERVAL '15 days', 14500.00, 1),
(CURRENT_TIMESTAMP, 15000.00, 1);

INSERT INTO HistoriquePrixVoyage (date_ecriture, prix_voyage, id_voyage) VALUES 
(CURRENT_TIMESTAMP - INTERVAL '60 days', 28000.00, 4),
(CURRENT_TIMESTAMP - INTERVAL '30 days', 29000.00, 4),
(CURRENT_TIMESTAMP, 30000.00, 4);