
-----10��26-11��2�Ѵ�����֤�û���    �˴Σ���������    �ˡ������ܼ�  �ң���֤ͨ����������    �ˡ������ܼ�   �ң�
SELECT 
SUM(tt.handler_cyz) AS '�Ѵ����������',
SUM(tt.handler_fbz) AS '�Ѵ��������ܼ�',
SUM(tt.ok_cyz) AS '��֤ͨ����������',
SUM(tt.ok_fbz) AS '��֤֤ͨ�������ܼ�'
FROM(
SELECT count(1) as handler_cyz,0 AS handler_fbz,0 AS ok_fbz,0 AS ok_cyz FROM `ssd_user_cyz` WHERE LEFT(register_time,10)>='2018-10-26' AND LEFT (register_time,10)<='2018-11-02' AND  state IN(3,4)
UNION
select 0 as handler_cyz, count(1) ashandler_fbz ,0 AS ok_fbz,0 AS ok_cyz FROM ylgj_user_fbz WHERE LEFT(register_time,10)>='2018-10-26' AND LEFT (register_time,10)<='2018-11-02' AND  state IN(3,4)
UNION
SELECT 0 as handler_cyz,0 AS handler_fbz,0 AS ok_fbz,count(1) AS ok_cyz  FROM `ssd_user_cyz` WHERE LEFT(register_time,10)>='2018-10-26' AND LEFT (register_time,10)<='2018-11-02' AND  state  IN(3)
UNION
select 0 as handler_cyz,0 AS handler_fbz,count(1) AS ok_fbz,0 AS ok_cyz FROM ylgj_user_fbz WHERE LEFT(register_time,10)>='2018-10-26' AND LEFT (register_time,10)<='2018-11-02' AND  state  IN(3)
 ) AS tt