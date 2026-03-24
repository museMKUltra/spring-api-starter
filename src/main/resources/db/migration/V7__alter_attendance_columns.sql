alter table attendance_label
    modify type varchar(20) not null;

alter table attendance_label
    drop column created_at;

alter table attendance_session
    modify status varchar(20) not null;

alter table work_summary
    modify status varchar(20) not null;
