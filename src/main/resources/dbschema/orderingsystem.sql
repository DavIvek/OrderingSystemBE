begin;

insert into public.userrole (name, description)
values ('ADMIN', 'Application admin') on conflict do nothing;
insert into public.userrole (name, description)
values ('DOCTOR', 'Doctor') on conflict do nothing;
insert into public.userrole (name, description)
values ('NURSE', 'Nurse') on conflict do nothing;
insert into public.userrole (name, description)
values ('PATIENT', 'Patient') on conflict do nothing;

commit;

begin;

-- Admin
insert into public.users (firstname, lastname, oib, username, email, phone_number, password, gender, role_id, user_type)
values ('Admin', 'admin', '69516633692', 'admin', 'admin@gmail.com', '0915551463',
        '$2a$10$7i0RFU5qjX.R/4iT2EeJgu7xh4mtPFX.ftYwqaFNnTUViAu3tWxNK', 'M', 1, 'admin') on conflict do nothing;

-- Doctors
insert into public.users (firstname, lastname, oib, username, email, phone_number, password, gender, role_id, user_type,
                          reservation_rule)
values ('Ivo', 'Ivić', '69516633699', 'ivoivic', 'ivo@gmail.com', '091197528',
        '$2a$12$TjdFClfh63WbIO405rsn5OC0gR0BQ4Yo7wA44lyIfxMNUEKoVaK0.', 'M', 2, 'doctor', 2) on conflict do nothing;

insert into public.users (firstname, lastname, oib, username, email, phone_number, password, gender, role_id, user_type,
                          reservation_rule)
values ('Sara', 'Sarić', '07121812083', 'sarasaric', 'sara@gmail.com', '098485559',
        '$2a$12$FPA6aE8kt1flTkyO0xH6NeD1lteuf/Y/2qnCRW3nDRLNcBqrIrBPG', 'F', 2, 'doctor', 2) on conflict do nothing;

insert into public.users (firstname, lastname, oib, username, email, phone_number, password, gender, role_id, user_type)
values ('Mate', 'Matić', '15182533057', 'matematic', 'mate@gmail.com', '098993045',
        '$2a$12$xi52EmeRe8h5TqRBdfKbEOSO7rp9TCihhQSaLU5czwTPBoLE07QVW', 'M', 3, 'nurse') on conflict do nothing;

-- Patients
insert into public.users (firstname, lastname, oib, username, email, phone_number, password, gender, role_id,
                          disappearances, notification, user_type, doctor_id)
values ('Andre', 'Flego', '51356916562', 'andre', 'andre@gmail.com', '098466057',
        '$2a$12$Xn6NmYAdOL4FxkhLB7BKReb3nuxMeHTtC1C.WSHkWxiFbbo/9i2ZS', 'M', 4, 0, 0, 'patient',
        2) on conflict do nothing;

insert into public.users (firstname, lastname, oib, username, email, phone_number, password, gender, role_id,
                          disappearances, notification, user_type, doctor_id)
values ('Renato', 'Majer', '44876336834', 'renato', 'renato@gmail.com', '095772728',
        '$2a$12$GE1.oJX8wmBp7/TNUatf1OaVEvzb.yDZpetzw/fCoY981OzmizdD.', 'M', 4, 0, 0, 'patient',
        3) on conflict do nothing;


commit;

--Medical services
begin;

insert into public.service (name, description, service_type)
values ('Vađenje krvi', 'Vađenje krvi', 'predefined_service') on conflict do nothing;

insert into public.service (name, description, service_type)
values ('Mjerenje tlaka', 'Mjerenje tlaka', 'predefined_service') on conflict do nothing;

insert into public.service (name, description, service_type, type)
values ('Pregled kirurga', 'Pregled kirurga', 'examination', 'specijalni') on conflict do nothing;

commit;


--Appointments
begin;

-- patient_id = 5
insert into public.appointments (start_time, end_time, medical_person_id, patient_id, medical_service_id)
values ('2023-01-08T12:30:00', '2023-01-08T13:30:00', 2, 5, 1) on conflict do nothing;

insert into public.appointments (start_time, end_time, medical_person_id, patient_id, medical_service_id)
values ('2023-01-04T12:30:00', '2023-01-04T13:30:00', 2, 5, 1) on conflict do nothing;

insert into public.appointments (start_time, end_time, medical_person_id, medical_service_id)
values ('2023-01-09T12:30:00', '2023-01-09T13:30:00', 2, 1) on conflict do nothing;

insert into public.appointments (start_time, end_time, medical_person_id, medical_service_id)
values ('2023-01-09T12:30:00', '2023-01-09T13:30:00', 4, 1) on conflict do nothing;

insert into public.appointments (start_time, end_time, medical_person_id, patient_id, medical_service_id)
values ('2023-01-08T12:30:00', '2023-01-08T13:30:00', 4, 5, 1) on conflict do nothing;

insert into public.appointments (start_time, end_time, medical_person_id, patient_id, medical_service_id)
values ('2023-01-13T10:00:00', '2023-01-13T10:30:00', 2, 5, 2) on conflict do nothing;

insert into public.appointments (start_time, end_time, medical_person_id, patient_id, medical_service_id)
values ('2023-01-20T09:30:00', '2023-01-20T12:30:00', 2, 5, 3) on conflict do nothing;

insert into public.appointments (start_time, end_time, medical_person_id, patient_id, medical_service_id)
values ('2023-01-12T11:30:00', '2023-01-12T12:00:00', 2, 5, 2) on conflict do nothing;

-- patient_id = 6
insert into public.appointments (start_time, end_time, medical_person_id, patient_id, medical_service_id)
values ('2023-01-21T16:00:00', '2023-01-21T17:00:00', 3, 6, 1) on conflict do nothing;

insert into public.appointments (start_time, end_time, medical_person_id, patient_id, medical_service_id)
values ('2023-01-08T10:30:00', '2023-01-08T11:00:00', 3, 6, 2) on conflict do nothing;

insert into public.appointments (start_time, end_time, medical_person_id, patient_id, medical_service_id)
values ('2023-01-15T08:00:00', '2023-01-15T10:30:00', 3, 6, 3) on conflict do nothing;


-- insert into public.medical_teams (name, valid_from, doctor_id, nurse_id)
-- values ('Kirurški tim', '2022-01-24T08:00:00', 2, 4) on conflict do nothing;
--
--
-- insert into public.appointments (start_time, end_time, medical_person_id, medical_team_id)
-- values ('2022-01-24T09:00:00', '2022-01-24T10:00:00', 2, 1) on conflict do nothing;



--available appointments for nurse_id = 4
insert into public.appointments (start_time, end_time, medical_person_id)
values ('2023-01-24T08:00:00', '2023-01-24T09:00:00', 4) on conflict do nothing;

insert into public.appointments (start_time, end_time, medical_person_id)
values ('2023-01-24T09:00:00', '2023-01-24T10:00:00', 4) on conflict do nothing;

insert into public.appointments (start_time, end_time, medical_person_id)
values ('2023-01-24T10:00:00', '2023-01-24T12:00:00', 4) on conflict do nothing;

--available appointments for doctor_id = 2

insert into public.appointments (start_time, end_time, medical_person_id)
values ('2023-01-24T12:00:00', '2023-01-24T14:00:00', 2) on conflict do nothing;

insert into public.appointments (start_time, end_time, medical_person_id)
values ('2023-01-25T14:00:00', '2023-01-25T14:30:00', 2) on conflict do nothing;

insert into public.appointments (start_time, end_time, medical_person_id)
values ('2023-01-24T15:30:00', '2023-01-24T16:00:00', 2) on conflict do nothing;

insert into public.appointments (start_time, end_time, medical_person_id)
values ('2023-01-24T16:30:00', '2023-01-24T17:00:00', 2) on conflict do nothing;

--available appointments for doctor_id = 3
insert into public.appointments (start_time, end_time, medical_person_id)
values ('2023-01-25T08:00:00', '2023-01-25T09:00:00', 3) on conflict do nothing;

insert into public.appointments (start_time, end_time, medical_person_id)
values ('2023-01-25T09:00:00', '2023-01-25T10:00:00', 3) on conflict do nothing;

insert into public.appointments (start_time, end_time, medical_person_id)
values ('2023-01-25T10:00:00', '2023-01-25T12:00:00', 3) on conflict do nothing;

insert into public.appointments (start_time, end_time, medical_person_id)
values ('2023-01-25T12:00:00', '2023-01-25T14:00:00', 3) on conflict do nothing;

insert into public.appointments (start_time, end_time, medical_person_id)
values ('2023-01-25T14:00:00', '2023-01-25T14:30:00', 3) on conflict do nothing;

insert into public.appointments (start_time, end_time, medical_person_id)
values ('2023-01-25T14:30:00', '2023-01-25T15:00:00', 3) on conflict do nothing;

insert into public.appointments (start_time, end_time, medical_person_id)
values ('2023-01-25T15:30:00', '2023-01-25T16:00:00', 3) on conflict do nothing;

commit;


