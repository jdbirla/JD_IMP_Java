# FastTableUpdate
Updating a table in the database can be time consuming. The fastest way would be the MERGE INTO sql ways. But there are certain limitations to this approach because of which it is awakward or difficult to use this. One of the road block I see in this approach is that in the MERGE INTO query there is ON (a.col1=b.col1), in this ON clause the column used cannot be set to new values. So, I felt the need of a way to have a lookup hashmap. Then the table I want to update from that table load all its rows in memory. Then perform batch update with the new values (updated column) by doing the lookup from the HashMap with the original values. This way it looks faster and more importantly we see the progress, which is very difficult in Oracle to while the query processing is in progress.

The steps this code perfroms are:

1. Expects its users to provide the DB Connection parameters on command line.
2. Then expects users to provide the query string which produce two columns table, one column represent the org_val and another updated_vals. Query from command line.
3. The runtime of the code loads the map using query provided in 2.
4. The expected table to update, the column name to update and the column name which indicates the key of the table, shall be proided by user via command line. 
5. The runtime will load the key columns and original column values from the target table as String[] keys and String[] vals. The two arrays of same size are used, the iteration index will remain same of keys array and vals array. In future this may be changed to use HashMap. Right now since I have fixed known size and I traverse one by one only so kept it simple data structure of arrays of same size.
6. The runtime will perform batch update using the prepared statements of jdbc while goign through each array items and for each vals it does the lookup from the map of step 3.

The update can be done two ways:<br>
A. For all the original values in the target column of the target table, do the lookup to get the updated value and then update the table based on key.<br>
B. For all the distinct original values in the target column of the target table, do the lookup to get the updated value and then update the table based on matching original values. Note: In this approach internally it creates an index (if not already present) on the target column and then delete that index.<br>

The approach B is faster as in one update it does changes in multiple rows and the loop runs over only on the distinct original values. <br>

<b> Usage : In the folder where tuf_0.0.1_exec.jar is present  </b> <br>
java -Xms5G -Xmx8G -jar tuf_0.0.1_exec.jar localhost sid 1521 dbuid password TABLENAME UNIQUE_NUMBER SURNAME "select a.col1, b.col2 from tablea a, tableb b where a.unique_number = b.unique_number "

<b> Approach B </b> <br>
Creating index Done! Time taken(ms)7254 <br>
Starting Update (by values) distint values:181 <br>
 batch update (by values) done: row count 3641966 distinct value count:181 <br>
Dropping index... <br>
Dropping index Done! <br>
Total Time(ms): LookupLoader = 13876 LoadOrgValues = 10872 Update = 183779 <br>

<b> Benchmark </b><br>
<b> Test1 BatchSize:autoCalc(distinctcount/4):  302542 TotalRowsToUpdate: 3486079 DistinctRowsToUpdate:1210168 Update(ms):1209662</b> <br>
[DEBUG] 2021/08/16 20:59:52 [main] update.TableUpdater -  batch update (by values) done: row count 3486079 distinct value count:1210168 <br>
[INFO ] 2021/08/16 20:59:57 [main] main.Client - Total Time(ms): LookupLoader =41718 LoadOrgValues = 28052 Update = 1209662 <br>
<b> Test2 BatchSize: manual set: 1000 TotalRowsToUpdate: 3486080 DistinctRowsToUpdate:1210169  Update(ms):1143445 </b><br>
[DEBUG] 2021/08/16 23:52:26 [main] update.TableUpdater -  batch update (by values) going on!1210000/1210169 batchsize:1000 <br>
[DEBUG] 2021/08/16 23:52:26 [main] update.TableUpdater -  batch update (by values) done: row count 3486080 distinct value count:1210169 <br>
[INFO ] 2021/08/16 23:52:30 [main] main.Client - Total Time(ms): LookupLoader =28228 LoadOrgValues = 8906 Update = 1143445<br>
<b> Test3 BatchSize: manual set: 10000 TotalRowsToUpdate: 3486079 DistinctRowsToUpdate:1210169 Update(ms):885751 </b><br>
[DEBUG] 2021/08/17 06:50:23 [main] update.TableUpdater -  batch update (by values) going on!1210000/1210169 batchsize:10000 <br>
[DEBUG] 2021/08/17 06:50:24 [main] update.TableUpdater -  batch update (by values) done: row count 3486080 distinct value count:1210169 <br>
[INFO ] 2021/08/17 06:50:38 [main] main.Client - Total Time(ms): LookupLoader =28211 LoadOrgValues = 14950 Update = 885751<br>
