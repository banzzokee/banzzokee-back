create user 'banzzokeeuser'@'127.0.0.1' identified by 'banzzokeepass';
create user 'banzzokeeuser'@'%' identified by 'banzzokeepass';
grant all privileges on banzzokee.* to 'banzzokeeuser'@'%';
flush privileges;