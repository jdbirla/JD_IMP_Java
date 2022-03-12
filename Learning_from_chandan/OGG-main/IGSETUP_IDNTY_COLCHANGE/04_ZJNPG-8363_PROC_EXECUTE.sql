--------------------------------------------------------------------
-- File Name	: 04_ZJNPG-8363_PROC_EXECUTE.sql
-- Description	: Execute Proc  CHANGE_IDENTITYCOL_TOSEQTYPE_PROC
-- Date			: 04 Dec 2020
-- Author		: Lishi Arora. 
-- Update   : 20210814:Chandan: Made it procedure.
--------------------------------------------------------------------
set serverouput on
BEGIN
EXECUTE VM1DTA.CHANGE_IDENTITYCOL_TOSEQTYPE_PROC;    
END;
/


---- OR try below command from SQLDEveloper
exec CHANGE_IDENTITYCOL_TOSEQTYPE_PROC;
