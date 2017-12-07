DROP TABLE ROLES;
DROP TABLE PRINCIPALS;

CREATE TABLE PRINCIPALS (
  PRINCIPALID VARCHAR(20), 
  PASSWORD VARCHAR(40)
)
ENGINE = InnoDB;

CREATE TABLE ROLES 
   ( PRINCIPALID VARCHAR(20), 
	 ROLENAME VARCHAR(100), 
	ROLEGROUP VARCHAR(100)
   )
ENGINE = InnoDB;

Insert into ROLES (PRINCIPALID,ROLENAME,ROLEGROUP) values ('admin','JBossAdmin','Roles');
Insert into ROLES (PRINCIPALID,ROLENAME,ROLEGROUP) values ('java','caller_java','CallerPrincipal');
Insert into ROLES (PRINCIPALID,ROLENAME,ROLEGROUP) values ('java','Echo','Roles');
Insert into ROLES (PRINCIPALID,ROLENAME,ROLEGROUP) values ('admin','HttpInvoker','Roles');
Insert into ROLES (PRINCIPALID,ROLENAME,ROLEGROUP) values ('kermit','thefrog','Roles');
Insert into ROLES (PRINCIPALID,ROLENAME,ROLEGROUP) values ('jduke','Echo','Roles');

Insert into PRINCIPALS (PRINCIPALID,PASSWORD) values ('java','echoman');
Insert into PRINCIPALS (PRINCIPALID,PASSWORD) values ('admin','admin');
Insert into PRINCIPALS (PRINCIPALID,PASSWORD) values ('kermit','friend');
Insert into PRINCIPALS (PRINCIPALID,PASSWORD) values ('jduke','theduke');
