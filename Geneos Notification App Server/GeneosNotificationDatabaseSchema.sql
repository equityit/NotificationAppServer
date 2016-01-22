create table domains(
id int not null auto_increment unique key,
domain varchar(100)
);

create table users(
userid int not null auto_increment unique key,
username varchar(100),
domainID int,
created_date datetime,
foreign key (domainID) references domains(id)
);

create table devices(
id int not null auto_increment unique key,
userid int,
Android_Id varchar(100),
Registration_key varchar(200),
verification_code int,
active varchar(1),
loggedin varchar(1),
foreign key (userid) references users(userid)
);

create table user_paths(
id int not null auto_increment unique key,
userid int,
XPath varchar(200),
foreign key (userid) references users(userid)
);

create table audit(
id int not null auto_increment unique key,
userid int,
process varchar(100),
date timestamp,
transfered_data varchar (1000)
);

create table activexpaths(
id int not null auto_increment unique key,
path varchar(2000)
);

Delimiter $$
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_Add_Dataview_To_User`(IN uname varchar(100),IN ent varchar(100),IN path varchar(500))
BEGIN
declare counter int  default (select id from user_paths where xpath like path);

if(counter is null) then
		insert into user_paths(userid, xpath) values((select userid from users where username = uname), path); 
end if;

END $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_Confirm_Valid_Domain`(IN checkDomain varchar(100))
BEGIN
select id from domains where domain like cast(trim(substring(checkDomain from locate('@', checkDomain))) as char(1000) character set utf8);
END $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_Create_Invalid_device`(IN usernameInput varchar(100),IN a_id varchar(100),IN r_id varchar(500), IN random int)
BEGIN
insert into devices(userid, android_id, Registration_key, verification_code, active, loggedin) values((select userid from users where username like usernameInput), a_id, r_id, random, 0, 0) ;
END $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_Create_New_User_With_Invalid_Device`(IN usernameInput varchar(100),IN a_id varchar(100),IN r_id varchar(500), IN random int)
BEGIN
insert into users(username, domainid, created_date) values(usernameInput, 
	(select id from domains where domain like cast(trim(substring(usernameInput from locate('@', usernameInput))) as char(1000) character set utf8)), 
    now());
insert into devices(userid, android_id, Registration_key, verification_code, active, loggedin) values((select userid from users where username like usernameInput), a_id, r_id, random, 0, 0) ;
END $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_get_live_devices`()
BEGIN
select u.username, d.android_id, d.Registration_key
from users as u 
join devices as d on u.userid = d.userid
where d.loggedin = 1
order by u.username DESC;
END $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_Get_User_Dataviews`(IN uname varchar(100))
BEGIN
select xpath from user_paths where userid = (select userid from users where username = uname);
END $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_logindevice`(in a_id varchar(200))
BEGIN
update devices set loggedin = 1 where android_id = a_id;
END $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_logoutdevice`(in a_id varchar(200))
BEGIN
update devices set loggedin = 0 where android_id = a_id;
END $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_remove_dataview_from_user`(in uname varchar(100), in path varchar (500))
BEGIN
delete from user_paths where userid = (select userid from users where username = uname) and xpath = path;
END $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_verify_device`(IN a_id varchar(100), IN verification int)
BEGIN
declare counter int  default (select verification_code from devices where android_id = a_id);
declare stat int  default (select active from devices where android_id = a_id);
if(counter is not null && stat != 1) then		
        if(verification = counter) then
        update devices set active=1 where android_id = a_id;
        end if;
end if;

END $$

delimiter ;

insert into domains(domain) values ('@Default');
insert into domains(domain) values ('@itrsgroup.com');
insert into users(username, domainID, created_date) values ('cmorley@itrsgroup.com', 1, now());