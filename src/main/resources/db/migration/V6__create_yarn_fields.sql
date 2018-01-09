ALTER TABLE public.projects ADD COLUMN yarn_pool_name TEXT,
                            ADD COLUMN yarn_max_cores INT,
                            ADD COLUMN yarn_max_memory_in_gb REAL;