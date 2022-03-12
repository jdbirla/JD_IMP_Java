# SubmitIGBatch
This is simple java program which submits IG batches from java.<br>

To use this program we need to set the QuipozConfig file and also the database.properties file, of IG. Suppose on a machine the IG batch server is already running. So on that machine, the IG batch server command line already taking these files as command line properties. Therefore, you can take those files from there or ask right person to get those files. You need to save these files on the local machine. 
<br>
You also need to store the csc-smart400-batch-1.0-SNAPSHOT.jar at local place and the accompanying "lib" folder of IG which has all the needed jars. 
<br> Example command line: <br>

java -Doracle.jdbc.autoCommitSpecCompliant=false -DQuipoz.CSCGroupAsiaWeb.XMLPath=H:/ckp/tools/allJars/ig/Config/QuipozCfg.xml -Ddbconfig=H:/ckp/tools/allJars/ig/Config/database.properties -cp "submitigbatch_vxx.jar;H:/ckp/tools/allJars/ig/csc-smart400-batch-1.0-SNAPSHOT.jar" base.SubmitAction ?action=status&bn=G1AUTOALOC&bd=20191209&uid=JPANRY&tout=1

<br>
<br>
When running from eclipse <br>
  The JVM Parameter:
-Doracle.jdbc.autoCommitSpecCompliant=false -DQuipoz.CSCGroupAsiaWeb.XMLPath=H:/ckp/tools/allJars/ig/Config/SIT01/QuipozCfg.xml  -Ddbconfig=H:/ckp/tools/allJars/ig/Config/SIT01/database.properties 
<br> 
The commad parameter:<br>
 ?action=status&bn=G1AUTOALOC&bd=20191209&uid=JPANRY&tout=1
 <br>
 Main class: <br>
 base.SubmitAction
 <br><br>

This will connect to SIT enrionment and check the status of the batch G1AUTOALOC, submitted by user JPANRY.
<br><br>
The result is returned as JSON: {"status":"00", "batchname":"bname","bizdate":"20190101 ", "userid":"jpaxx","tout":"1"}

[20200328] In command line argument, below paramters seems needed: <br>
classpath:/com/csc/groupasia/context/GroupContext.xml classpath:/com/csc/groupasia/context/dao/Group-DAO-0.xml classpath*:com/zurich/customerZurJpn-services.xml
