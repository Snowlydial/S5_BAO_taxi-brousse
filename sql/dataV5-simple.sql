\c taxibroussedb;

INSERT INTO Caisse (libelle) VALUES 
('Espece'),
('Mobile Money (MVola)'),
('Carte Bancaire');

--?=== Bus Configuration
INSERT INTO BusConf (libelle, valeur) VALUES 
('nb_place_standard', '100'),
('nb_place_premium', '100'),
('nb_place_vip', '100'),
('climatisation', 'oui'),
('wifi', 'oui');

--?=== Bus
INSERT INTO Bus (immatriculation) VALUES ('TBK1224');

-- Associate Bus with configuration (100 standard places, AC, wifi)
INSERT INTO Bus_BusConf (id_bus, id_busConf) VALUES 
(1, 1),  -- 100 standard places
(1, 4),  -- AC
(1, 5);  -- wifi

--?=== Base Configuration Data
INSERT INTO ClassePlace (libelle, prix_place) VALUES 
('Standard', 80000),
('Premium', 140000),
('VIP', 180000);

INSERT INTO CategorieGroupeAge (libelle) VALUES 
('Enfant (0-12 ans)'),
('Adulte (18-59 ans)'),
('Senior (60+ ans)');

--?=== Gares
INSERT INTO Gare (libelle) VALUES 
('Antananarivo'),
('Toamasina'),
('Majunga');

--?=== Voyages
INSERT INTO Voyage (duree, prix_voyage, id_gare_1, id_gare_2) VALUES 
(2.0, 40000.00, 1, 2);  -- TNR -> Toamasina (2h)

--?=== ClasseAge_Conf
-- ENFANT pricing for TNR -> Toamasina (voyage_id = 1)
INSERT INTO ClasseAge_Conf (valeur_override, est_pourcentage, date_debut, date_fin, id_categoriegroupeage, id_classeplace, id_voyage) 
VALUES 
(40000, FALSE, NULL, NULL, 1, 1, 1),  -- Child Standard for TNR->Toamasina
(50000, FALSE, NULL, NULL, 1, 2, 1),  -- Child Premium for TNR->Toamasina
(65000, FALSE, NULL, NULL, 1, 3, 1);  -- Child VIP for TNR->Toamasina

-- ADULTE prices for TNR -> Toamasina (voyage_id = 1)
INSERT INTO ClasseAge_Conf (valeur_override, est_pourcentage, date_debut, date_fin, id_categoriegroupeage, id_classeplace, id_voyage) 
VALUES 
(50000, FALSE, NULL, NULL, 2, 1, 1),  -- Adult Standard for TNR->Toamasina
(60000, FALSE, NULL, NULL, 2, 2, 1),  -- Adult Premium for TNR->Toamasina
(70000, FALSE, NULL, NULL, 2, 3, 1);  -- Adult VIP for TNR->Toamasina

-- SENIOR pricing for TNR -> Toamasina (voyage_id = 1) - 20% discount
INSERT INTO ClasseAge_Conf (valeur_override, est_pourcentage, date_debut, date_fin, id_categoriegroupeage, id_classeplace, id_voyage) 
VALUES 
(-0.20, TRUE, NULL, NULL, 3, 1, 1),  -- Senior Standard for TNR->Toamasina (20% off adult price)
(-0.20, TRUE, NULL, NULL, 3, 2, 1),  -- Senior Premium for TNR->Toamasina (20% off adult price)
(-0.20, TRUE, NULL, NULL, 3, 3, 1);  -- Senior VIP for TNR->Toamasina (20% off adult price)

--?=== Bus Voyages
INSERT INTO Bus_Voyage (heure_depart, date_depart, prix_specifique, id_bus, id_voyage) VALUES
('10:00:00', '2026-01-20', NULL, 1, 1),  -- 1er busVoyage: 20-01-26, 10h
('10:00:00', '2026-01-21', NULL, 1, 1),  -- 2eme busVoyage: 21-01-26, 10h
('15:00:00', '2026-01-21', NULL, 1, 1);  -- 3eme busVoyage: 21-01-26, 15h

--?=== Clients (sample adults for reservations)
INSERT INTO CategorieGenre (libelle) VALUES 
('Homme'),
('Femme');

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

--?=== 1er Bus Voyage: 2 Diffusions (Vaniala, Lewis)
-- 1 Diffusion Vaniala
INSERT INTO Diffusion (date_diffusion, heure_diffusion, id_societe, id_bus_voyage, description) VALUES
('2026-01-20', '09:00:00', 1, 1, 'Pub Vaniala - 1er voyage');

-- 1 Diffusion Lewis
INSERT INTO Diffusion (date_diffusion, heure_diffusion, id_societe, id_bus_voyage, description) VALUES
('2026-01-20', '09:30:00', 2, 1, 'Pub Lewis - 1er voyage');

--?=== 2eme Bus Voyage: 3 Diffusions (2 Socobis, 1 Jejoo)
-- 2 Diffusions Socobis
INSERT INTO Diffusion (date_diffusion, heure_diffusion, id_societe, id_bus_voyage, description) VALUES
('2026-01-21', '09:00:00', 3, 2, 'Pub Socobis - 2eme voyage #1'),
('2026-01-21', '09:15:00', 3, 2, 'Pub Socobis - 2eme voyage #2');

-- 1 Diffusion Jejoo
INSERT INTO Diffusion (date_diffusion, heure_diffusion, id_societe, id_bus_voyage, description) VALUES
('2026-01-21', '09:30:00', 4, 2, 'Pub Jejoo - 2eme voyage');

--?=== 3eme Bus Voyage: 0 Diffusions
