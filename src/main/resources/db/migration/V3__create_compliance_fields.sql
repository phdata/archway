ALTER TABLE public.projects ADD COLUMN phi_data BOOLEAN,
                            ADD COLUMN pci_data BOOLEAN,
                            ADD COLUMN pii_data BOOLEAN;

UPDATE public.projects SET phi_data = false,
                           pci_data = false,
                           pii_data = false;

ALTER TABLE public.projects ALTER COLUMN phi_data SET NOT NULL,
                            ALTER COLUMN pci_data SET NOT NULL,
                            ALTER COLUMN pii_data SET NOT NULL;