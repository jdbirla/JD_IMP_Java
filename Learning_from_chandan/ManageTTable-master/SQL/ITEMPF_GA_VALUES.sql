set verify off
set sqlformat csv

prompt "*********************[	VM1DTA.ITEMPF GENAREA WITH HASHED GENAREA	]**********************************************************************************************"
select ITEMPFX, ITEMCOY, ITEMTABL, ITEMITEM, ITEMSEQ, TABLEPROG, VALIDFLAG, ITMFRM, ITMTO,
UTL_RAW.cast_to_varchar2(GENAREA), ORA_HASH(GENAREA) HGA 
from VM1DTA.ITEMPF 
where itemtabl = '&1' 
AND VALIDFLAG = '1'
ORDER BY ITEMPFX, ITEMCOY, ITEMTABL, ITEMITEM, ITEMSEQ,TABLEPROG, HGA, ITMFRM, ITMTO
 ;

exit;