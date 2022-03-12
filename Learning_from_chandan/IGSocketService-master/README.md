# IGSocketService
A Socket Program to keep listening the request of batch submit. Since this code uses: submitigbatch_xxx.jar, please refer the git repository for that batch, or download the jar from there. <br>

<b>1	REQUIRED FILES AND INFORMATION </b> <br>
1.1	JARS: IGSOCKETSERVICE_1.0.3.JAR <br>
1.2	JARS: SUBMITIGBATCH_2.3.0.JAR <br>
1.3	KNOW THE PATH OF FOLLOWING FILES ON SERVER<br>

1.3.1	Config Files: Get path of QuipozConfig.xml <br>
1.3.2	Properties Files: Get path of database.properties <br>

<b>2	PREPARATION STEP</b><br>
2.1	PUT THE JARS ON THE SERVER<br>
Place following two jars on the IG Batch Server: <br>
2.1.1	igsocketservice_1.0.3.jar <br>
2.1.2	submitigbatch_2.3.0.jar <br>
Let us say the folder is /home/jpacpr/igsocketservice <br>

<b>3	EXECUTION </b><br>
Go to the folder where you have saved the two jars say /home/jpacpr/igsocketservice. Then run the below command line: <br>
java -Doracle.jdbc.autoCommitSpecCompliant=false -DQuipoz.CSCGroupAsiaWeb.XMLPath=H:/ckp/tools/allJars/ig/Config/QuipozCfg.xml -Ddbconfig=H:/ckp/tools/allJars/ig/Config/database.properties -cp H:/ckp/tools/allJars/ig/csc-smart400-batch-1.0-SNAPSHOT.jar;H:/ckp/tools/myexport/SubmitIgBatch/submitigbatch_2.3.0.jar;H:/ckp/tools/myexport/IGSocketService/igsocketservice_1.0.3.jar core.Server 2020
 <br> <br>
Substitute the value of <PathOfConfig.xml> And <PathOfdatabase.properties>. It may look like below <br>
Example on <b>AIX:</b> /usr/IBM/WebSphere/AppServer/java/8.0/bin/java -Doracle.jdbc.autoCommitSpecCompliant=false -DQuipoz.CSCGroupAsiaWeb.XMLPath=/opt/ig/conf/PTX22/QuipozCfg_PTX22.xml -Ddbconfig=/opt/ig/conf/PTX22/database.properties -cp /opt/ig/batchjob/PTX21/csc-smart400-batch/csc-smart400-batch-1.0-SNAPSHOT.jar:/home/jpacpr/javawork/IGSocketService/submitigbatch_2.3.0.jar:/home/jpacpr/javawork/IGSocketService/igsocketservice_1.0.3.jar core.Server 2020
 <br>
The client command line to test: <br>
/usr/IBM/WebSphere/AppServer/java/8.0/bin/java -cp /home/jpacpr/javawork/IGSocketService/igsocketservice_1.0.2.jar core.Client 127.0.0.1 2020 action=status bn=G1AUTOALOC bd=20250901 uid=BATCHUSER tout=1 
