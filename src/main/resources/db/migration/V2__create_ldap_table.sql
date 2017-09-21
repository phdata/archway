ALTER TABLE public.projects ADD COLUMN system_name text;
UPDATE public.projects SET system_name = name;
ALTER TABLE public.projects ALTER COLUMN system_name SET NOT NULL;

ALTER TABLE public.projects ADD COLUMN ldap_dn text;