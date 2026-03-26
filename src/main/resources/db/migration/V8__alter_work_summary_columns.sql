alter table work_summary
    modify hourly_rate decimal(10, 2) null;

alter table work_summary
    modify salary_amount decimal(12, 2) default 0.00 null;

