alter table attendance_session
    drop foreign key fk_session_label;

alter table attendance_session
    add constraint fk_session_label
        foreign key (label_id) references attendance_label (id)
            on delete set null;

