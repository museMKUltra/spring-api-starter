alter table attendance_label
    add user_id bigint null;

alter table attendance_label
    add deleted_at datetime null;

alter table attendance_label
    add constraint attendance_label_users_id_fk
        foreign key (user_id) references users (id);

