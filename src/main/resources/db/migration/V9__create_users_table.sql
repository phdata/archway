CREATE TABLE public.users(
    username text NOT NULL,
    database text NOT NULL,
    data_directory text NOT NULL,
    role text NOT NULL,
    CONSTRAINT users_pk PRIMARY KEY (username)
);