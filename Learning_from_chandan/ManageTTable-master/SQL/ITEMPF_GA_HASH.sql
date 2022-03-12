set verify off
set sqlformat csv

prompt "*********************[	VM1DTA.ITEMPF stat of GENAREA TABLES	]**********************************************************************************************"
WITH T AS (
	select ITEMPFX, ITEMCOY, ITEMTABL, ITEMITEM, ITEMSEQ, TABLEPROG, ITMFRM, ITMTO,
	UTL_RAW.cast_to_varchar2(GENAREA) GA
	from VM1DTA.ITEMPF WHERE VALIDFLAG = '1'
) 
SELECT ITEMPFX, ITEMCOY, ITEMTABL, ITEMITEM, ITEMSEQ, TABLEPROG, ITMFRM, ITMTO, ORA_HASH(GA) HGA
FROM T 
WHERE TRIM(GA) is not null
ORDER BY ITEMPFX, ITEMCOY, ITEMTABL, ITEMITEM, ITEMSEQ,TABLEPROG, HGA, ITMFRM, ITMTO
 ;

exit;