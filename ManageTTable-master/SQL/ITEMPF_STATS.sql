set verify off
set sqlformat csv

prompt "*********************[	VM1DTA.ITEMPF stat of all items in ITEMPF	]**********************************************************************************************"
WITH T AS (
select ITEMPFX, ITEMCOY, ITEMTABL, ITEMITEM, ITEMSEQ, TABLEPROG, VALIDFLAG, ITMFRM, ITMTO,
UTL_RAW.cast_to_varchar2(GENAREA) GA
from VM1DTA.ITEMPF WHERE VALIDFLAG = '1' 
) SELECT ITEMPFX, ITEMCOY, ITEMTABL, count(1) as CNT FROM T GROUP BY ITEMPFX, ITEMCOY, ITEMTABL

order by ITEMPFX, ITEMCOY, ITEMTABL, TABLEPROG
 ; 
exit;