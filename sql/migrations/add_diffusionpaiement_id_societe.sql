-- Add id_societe to diffusionpaiement to allow society-level payments/credits
ALTER TABLE diffusionpaiement
  ADD COLUMN id_societe INTEGER;

ALTER TABLE diffusionpaiement
  ADD CONSTRAINT fk_diffusionpaiement_societe
    FOREIGN KEY (id_societe) REFERENCES societe(id_societe);
