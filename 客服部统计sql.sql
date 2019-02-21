
-----10月26-11月2已处理认证用户共    人次（货车导航    人、运力管家  家，认证通过货车导航    人、运力管家   家）
SELECT 
SUM(tt.handler_cyz) AS '已处理货车导航',
SUM(tt.handler_fbz) AS '已处理运力管家',
SUM(tt.ok_cyz) AS '认证通过货车导航',
SUM(tt.ok_fbz) AS '认证证通过运力管家'
FROM(
SELECT count(1) as handler_cyz,0 AS handler_fbz,0 AS ok_fbz,0 AS ok_cyz FROM `ssd_user_cyz` WHERE LEFT(register_time,10)>='2018-10-26' AND LEFT (register_time,10)<='2018-11-02' AND  state IN(3,4)
UNION
select 0 as handler_cyz, count(1) ashandler_fbz ,0 AS ok_fbz,0 AS ok_cyz FROM ylgj_user_fbz WHERE LEFT(register_time,10)>='2018-10-26' AND LEFT (register_time,10)<='2018-11-02' AND  state IN(3,4)
UNION
SELECT 0 as handler_cyz,0 AS handler_fbz,0 AS ok_fbz,count(1) AS ok_cyz  FROM `ssd_user_cyz` WHERE LEFT(register_time,10)>='2018-10-26' AND LEFT (register_time,10)<='2018-11-02' AND  state  IN(3)
UNION
select 0 as handler_cyz,0 AS handler_fbz,count(1) AS ok_fbz,0 AS ok_cyz FROM ylgj_user_fbz WHERE LEFT(register_time,10)>='2018-10-26' AND LEFT (register_time,10)<='2018-11-02' AND  state  IN(3)
 ) AS tt