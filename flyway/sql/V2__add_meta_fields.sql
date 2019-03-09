alter table workspace_request
  add column summary varchar(255),
  add column description TEXT,
  add column behavior varchar(50);

update workspace_request
set    behavior = new_behavior,
       summary = b.name,
       description = b.name
from   (select case
                 when single_user = true then 'user'
                 when db_count = 3 then 'structured'
                 else 'simple'
               end as new_behavior,
               wr.name,
               wr.id
        from   workspace_request wr
               inner join (select Count(*) as db_count,
                                  workspace_request_id
                           from   workspace_database
                           group  by workspace_request_id) a
                       on wr.id = a.workspace_request_id) b
where  b.id = workspace_request.id;

alter table workspace_request
  alter column summary set not null,
  alter column description set not null,
  alter column behavior set not null;