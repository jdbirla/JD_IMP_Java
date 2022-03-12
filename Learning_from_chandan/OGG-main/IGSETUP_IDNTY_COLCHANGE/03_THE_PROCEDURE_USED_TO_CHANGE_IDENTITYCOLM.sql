create or replace PROCEDURE CHANGE_IDENTITYCOL_TOSEQTYPE_PROC
AUTHID CURRENT_USER

AS
maxUnqNum NUMBER;
stmt VARCHAR2(500);
table_nm VARCHAR(100);
column_nm VARCHAR(100);


 CURSOR table_names IS SELECT TABLE_NAME,COLUMN_NAME FROM IDENTITY_UNIQUE_NO_TMP where IDENTITY_COLUMN='YES';

begin

  FOR cur_tale_nm IN table_names
LOOP

table_nm := cur_tale_nm.table_name;
column_nm := cur_tale_nm.COLUMN_NAME;

dbms_output.put_line(' Table Name is ' || table_nm);
maxUnqNum :=0;

 ---------------------- STEP 1/5 ------------------------------------------------------------
begin
  dbms_output.put_line(' Getting max number: Table Name is ' || table_nm||' MaxUniNo is ' || maxUnqNum);
  stmt := 'select NVL(max(' ||column_nm||')+1,1) from ' || table_nm ;
  execute immediate stmt into maxUnqNum ;-- The max unqiue number of the target table obtained.


  dbms_output.put_line(' Table Name is ' || table_nm||' MaxUniNo is ' || maxUnqNum);



    ---------------------- STEP 2/5 ------------------------------------------------------------
  stmt := 'CREATE SEQUENCE  "VM1DTA"."SEQ_GG_'||table_nm||'"  MINVALUE 1 MAXVALUE 99999999999999999999 INCREMENT BY 1 START WITH ' || maxUnqNum ||' NOCACHE  NOORDER  NOCYCLE  NOKEEP  NOSCALE  GLOBAL ';
  execute immediate stmt ;-- -- Identity dropped
  dbms_output.put_line(' Sequence got created ' || '"VM1DTA"."SEQ_GG_'||table_nm );

    ---------------------- STEP 3/5 ------------------------------------------------------------
     -- dbms_output.put_line(table_nm || ' + '|| column_nm || ' +  ' ||table_nm);

  stmt := 'ALTER TABLE '||table_nm||' MODIFY ' ||column_nm||' DROP IDENTITY ';
  execute immediate stmt ;-- The sequence is generated.
  dbms_output.put_line(' identity for unique_no dropped ' || '"VM1DTA"."SEQ_GG_'||table_nm );


    ---------------------- STEP 4/5 ------------------------------------------------------------
      --dbms_output.put_line(table_nm || ' + '|| column_nm || ' +  ' ||table_nm);

  stmt := 'ALTER TABLE ' ||table_nm ||' MODIFY (' ||column_nm||' DEFAULT SEQ_GG_'||table_nm||'.nextval)';
  execute immediate stmt ;-- The column unique_number is modifed to take the values from sequence.
  dbms_output.put_line(' The unique number column modified to use seq.nextval ' );


   ---------------------- STEP 5/5 ------------------------------------------------------------
  BEGIN
  UPDATE IDENTITY_UNIQUE_NO_TMP SET IDENTITY_COLUMN='NO' WHERE TABLE_NAME = table_nm;
  END;--Update identity column as No
  dbms_output.put_line('Update identity column as No for table ' );
Exception
   when others 
   then 
    dbms_output.put_line('Since column name is not UNIQUE_NUMBER so just printing it. ' );
--    UPDATE IDENTITY_UNIQUE_NO_TMP SET COLUMN_NAME= column_nm WHERE TABLE_NAME = table_nm;
  END;


  END LOOP;

  end;