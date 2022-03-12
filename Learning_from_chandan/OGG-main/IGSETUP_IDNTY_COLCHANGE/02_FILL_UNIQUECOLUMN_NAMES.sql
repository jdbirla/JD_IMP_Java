--------------------------------------------------------------------
-- Description	: This will insert the table and columns which are of type generate "identity" always into "VM1DTA"."IDENTITY_UNIQUE_NO_TMP"
-- Date			: 04 Dec 2020
-- Author		: Lishi Arora. 
-- Update   : 20210814:Chandan: Identify the columns and insert into "VM1DTA"."IDENTITY_UNIQUE_NO_TMP" with scripts.
--------------------------------------------------------------------

--1. With query fill all the identity columns in the tmp table

--insert into IDENTITY_UNIQUE_NO_TMP  (select TABLE_NAME, 'UNIQUE_NUMBER' as COLUMN_NAME, 'YES' as IDENTITY_COLUMN  from dba_tables where owner='VM1DTA' and has_identity='YES' );
  insert into "VM1DTA"."IDENTITY_UNIQUE_NO_TMP" (
select  a.TABLE_NAME, b.COLUMN_NAME as COLUMN_NAME, b.IDENTITY_COLUMN  
    from dba_tables a, dba_tab_columns b
    where a.table_name=b.table_name and a.has_identity = 'YES' and b.IDENTITY_COLUMN='YES'
);

--2. Some of the above tables identity column names are not 'UNIQUE_NUMBER' hence correct its name manually. 
/*
UPDATE IDENTITY_UNIQUE_NO_TMP SET COLUMN_NAME='RECIDXCLIENT' where table_name='ZDCLPF';
UPDATE IDENTITY_UNIQUE_NO_TMP SET COLUMN_NAME='RECIDXPOLICY' where table_name='ZDRPPF';
UPDATE IDENTITY_UNIQUE_NO_TMP SET COLUMN_NAME='UNIQUENUMBER' where table_name='ZTENPF';
UPDATE IDENTITY_UNIQUE_NO_TMP SET COLUMN_NAME='RECIDXOTHERS' where table_name='ZDROPF';
UPDATE IDENTITY_UNIQUE_NO_TMP SET COLUMN_NAME='RECIDXLETTERS' where table_name='ZDLTPF';
UPDATE IDENTITY_UNIQUE_NO_TMP SET COLUMN_NAME='RECIDXOKEROR' where table_name='ZDOEPF';
UPDATE IDENTITY_UNIQUE_NO_TMP SET COLUMN_NAME='RECIDXBILLING' where table_name='ZDRFPF';
UPDATE IDENTITY_UNIQUE_NO_TMP SET COLUMN_NAME='RECIDXBILLING' where table_name='ZDRBPF';
UPDATE IDENTITY_UNIQUE_NO_TMP SET COLUMN_NAME='RECIDXPOLTRNH' where table_name='ZDPTPF';
UPDATE IDENTITY_UNIQUE_NO_TMP SET COLUMN_NAME='RECIDXCOLRES' where table_name='ZDCRPF';
*/
