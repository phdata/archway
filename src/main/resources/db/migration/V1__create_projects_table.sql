CREATE TABLE public.projects(
    id bigserial NOT NULL,
    name text NOT NULL,
    purpose text NOT NULL,
    created timestamp NOT NULL,
    created_by text NOT NULL,
    CONSTRAINT projects_pk PRIMARY KEY (id)
);