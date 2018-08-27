-----------------
-- T A B L E S --
-----------------
CREATE TABLE T_TEST_IDENT (
  IDENT            VARCHAR(11) PRIMARY KEY,
  TILHOERER_GRUPPE NUMBER(9) NOT NULL
);

CREATE TABLE T_GRUPPE (
  ID             NUMBER(9) PRIMARY KEY,
  NAVN           VARCHAR2(50) NOT NULL,
  HENSIKT        VARCHAR2(200) NOT NULL,
  OPPRETTET_AV   VARCHAR2(10) NOT NULL,
  DATO_ENDRET    DATE    NOT NULL,
  SIST_ENDRET_AV VARCHAR2(10) NOT NULL,
  TILHOERER_TEAM NUMBER(9)    NOT NULL
);

CREATE TABLE T_TEAM (
  ID             NUMBER(9) PRIMARY KEY,
  NAVN           VARCHAR2(50) NOT NULL UNIQUE,
  BESKRIVELSE    VARCHAR2(200),
  DATO_OPPRETTET DATE    NOT NULL,
  EIER           VARCHAR2(10) NOT NULL
);

CREATE TABLE T_BRUKER (
  NAV_IDENT       VARCHAR2(10) PRIMARY KEY
);

CREATE TABLE T_TEAM_MEDLEMMER (
  TEAM_ID   NUMBER(9) NOT NULL,
  BRUKER_ID VARCHAR2(10) NOT NULL
);

CREATE TABLE T_BRUKER_FAVORITTER (
  GRUPPE_ID   NUMBER(9) NOT NULL,
  BRUKER_ID VARCHAR2(10) NOT NULL
);

CREATE TABLE T_BESTILLING_PROGRESS (
  ID                NUMBER(9) PRIMARY KEY,
  BESTILLING_ID     NUMBER(9) NOT NULL,
  IDENT             NUMBER(11) NOT NULL,
  TPSF_SUCCESS_ENVIRONMENTS VARCHAR2(200),
  SIGRUN_SUCCESS_ENVIRONMENTS VARCHAR2(200),
  AAREG_SUCCESS_ENVIRONMENTS VARCHAR2(200),
  FEIL             VARCHAR(999)
);

CREATE TABLE T_BESTILLING (
  ID NUMBER(9) PRIMARY KEY,
  GRUPPE_ID NUMBER(9) NOT NULL,
  MILJOER VARCHAR2(200) NOT NULL,
  ANTALL_IDENTER NUMBER(5)  NOT NULL,
  SIST_OPPDATERT TIMESTAMP NOT NULL,
  FERDIG CHAR(1) NOT NULL
);

---------------------------------------------------
-- F O R E I G N   K E Y   C O N S T R A I N T S --
---------------------------------------------------
ALTER TABLE T_BESTILLING_PROGRESS
  ADD CONSTRAINT PROGRESS_BESTILLING_FK FOREIGN KEY (BESTILLING_ID) REFERENCES T_BESTILLING (ID);

ALTER TABLE T_TEST_IDENT
  ADD CONSTRAINT TESTPERSON_GRUPPE_FK FOREIGN KEY (TILHOERER_GRUPPE) REFERENCES T_GRUPPE (ID);

ALTER TABLE T_GRUPPE
  ADD CONSTRAINT GRUPPE_TEAM_FK FOREIGN KEY (TILHOERER_TEAM) REFERENCES T_TEAM (ID);

ALTER TABLE T_GRUPPE
  ADD CONSTRAINT GRUPPE_ENDRET_AV_FK FOREIGN KEY (SIST_ENDRET_AV) REFERENCES T_BRUKER (NAV_IDENT);

ALTER TABLE T_GRUPPE
  ADD CONSTRAINT GRUPPE_OPPRETTET_AV_FK FOREIGN KEY (OPPRETTET_AV) REFERENCES T_BRUKER (NAV_IDENT);

ALTER TABLE T_TEAM
  ADD CONSTRAINT TEAMETS_EIER_FK FOREIGN KEY (EIER) REFERENCES T_BRUKER (NAV_IDENT);

ALTER TABLE T_TEAM_MEDLEMMER
  ADD CONSTRAINT TEAM_MANYTOMANY_FK FOREIGN KEY (TEAM_ID) REFERENCES T_TEAM (ID);

ALTER TABLE T_TEAM_MEDLEMMER
  ADD CONSTRAINT MEDLEMMER_MANYTOMANY_FK FOREIGN KEY (BRUKER_ID) REFERENCES T_BRUKER (NAV_IDENT);

ALTER TABLE T_BRUKER_FAVORITTER
  ADD CONSTRAINT BRUKER_MANYTOMANY_FK FOREIGN KEY (BRUKER_ID) REFERENCES T_BRUKER (NAV_IDENT);

ALTER TABLE T_BRUKER_FAVORITTER
  ADD CONSTRAINT FAVORITTER_GRUPPER_FK FOREIGN KEY (GRUPPE_ID) REFERENCES T_GRUPPE (ID);

---------------------------------------------
-- ADDITIONAL CONSTRAINTS FOR DATA QUALITY --
---------------------------------------------

-----------------------
-- S E Q U E N C E S --
-----------------------
CREATE SEQUENCE T_GRUPPE_SEQ START WITH 1;
CREATE SEQUENCE T_TEAM_SEQ START WITH 1;
CREATE SEQUENCE T_BESTILLING_SEQ START WITH 1;
CREATE SEQUENCE T_BESTILLING_PROGRESS_SEQ START WITH 1;

