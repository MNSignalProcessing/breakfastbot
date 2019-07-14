-- :name create-members-table :!
create table members (
  id int primary key generated by default as identity,
  email text unique,
  fullname varchar(40),
  active boolean not null
);

-- :name drop-members-table :!
drop table if exists members;

-- :name insert-member :<! :n
insert into members (email, fullname, active)
values (:email, :fullname, true)
returning id;

-- :name get-members :?
select * from members;

-- :name get-active-members :?
select id, email, fullname from members where active = true;

-- :name get-member-by-id :? :1
select fullname, email from members where id = :id;

-- :name change-member-active :! :1
update members set active = :active where email = :email;

-- :name get-member-by-email :? :1
select email, fullname, active from members where email = :email;

-- :name update-member-fullname :! :1
update members set fullname = :fullname where email = :email;
