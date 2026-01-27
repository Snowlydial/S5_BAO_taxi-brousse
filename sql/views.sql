\c taxibroussedb;

-- Views to compute invoice (facture) aggregates for faster / centralized reads.
-- Note: This uses FactureLigne (saved lines) and Diffusion / DiffusionConf / DiffusionPaiement
-- to compute totals. The diffusion pricing join assumes DiffusionConf ranges cover diffusion dates.

CREATE OR REPLACE VIEW view_facture_line_totals AS
SELECT
  f.id_facture AS facture_id,
  COALESCE(SUM(CASE WHEN fl.type_ligne = 'RESERVATION' THEN fl.montant END), 0) AS total_reservations,
  COALESCE(SUM(CASE WHEN fl.type_ligne = 'DIFFUSION' THEN fl.montant END), 0) AS total_diffusions,
  COALESCE(SUM(fl.montant), 0) AS total_lignes
FROM Facture f
LEFT JOIN FactureLigne fl ON fl.id_facture = f.id_facture
GROUP BY f.id_facture;

-- Summarize diffusion amounts per facture (based on the Bus_Voyage linked to the Facture)
-- total_due_diffusions: sum of matching DiffusionConf.prix for each Diffusion (matched by date range)
-- total_paid_diffusions: sum of DiffusionPaiement.montant_paye for diffusions attached to the same Bus_Voyage
CREATE OR REPLACE VIEW view_facture_diffusion_summary AS
SELECT
  f.id_facture AS facture_id,
  COALESCE(SUM(dc.prix), 0) AS total_due_diffusions,
  COALESCE(SUM(dp.montant_paye), 0) AS total_paid_diffusions,
  (COALESCE(SUM(dc.prix), 0) - COALESCE(SUM(dp.montant_paye), 0)) AS total_remaining_diffusions
FROM Facture f
LEFT JOIN Diffusion d ON d.id_bus_voyage = f.id_bus_voyage
LEFT JOIN DiffusionConf dc ON d.date_diffusion BETWEEN dc.date_debut AND dc.date_fin
LEFT JOIN DiffusionPaiement dp ON dp.id_diffusion = d.id_diffusion
GROUP BY f.id_facture;

-- Combined view exposing the most useful totals for a Facture row
CREATE OR REPLACE VIEW view_facture_totals AS
SELECT
  l.facture_id,
  l.total_reservations,
  s.total_due_diffusions,
  s.total_paid_diffusions,
  s.total_remaining_diffusions,
  -- montant_total (consumed by the UI) = reservations CA + paid diffusions (what has actually been billed/received)
  (l.total_reservations + s.total_paid_diffusions) AS montant_total
FROM view_facture_line_totals l
LEFT JOIN view_facture_diffusion_summary s ON s.facture_id = l.facture_id;
