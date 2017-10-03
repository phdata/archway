ALTER TABLE public.projects ADD COLUMN hdfs_location TEXT,
                            ADD COLUMN hdfs_requested_size_in_gb REAL;

UPDATE public.projects SET hdfs_requested_size_in_gb = .5;

ALTER TABLE public.projects ALTER COLUMN hdfs_requested_size_in_gb SET NOT NULL;
