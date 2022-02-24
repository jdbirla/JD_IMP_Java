--------------------------------------------------------------------
-- File Name	: 01_IDENTITY_UNIQUE_NO_TMP_ZJNPG-8363_TABLE_CREATE.sql
-- Description	: Table IDENTITY_UNIQUE_NO_TMP
-- Date			: 04 Dec 2020
-- Author		: Lishi Arora
--------------------------------------------------------------------

CREATE TABLE "VM1DTA"."IDENTITY_UNIQUE_NO_TMP"
  (
    "TABLE_NAME"      VARCHAR2(100 CHAR),
    "COLUMN_NAME"     VARCHAR2(100 CHAR) DEFAULT 'UNIQUE_NUMBER',
    "IDENTITY_COLUMN" VARCHAR2(3 CHAR) DEFAULT 'YES',
    PRIMARY KEY ("TABLE_NAME")
  )
  PARALLEL NOLOGGING;
