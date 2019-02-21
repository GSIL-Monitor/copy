package com.shabro.erp.consignor.resource;

import com.jfinal.aop.Before;
import com.jfinal.json.Json;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

import com.lastb7.dubbo.plugin.spring.Inject;
import com.lastb7.dubbo.plugin.spring.IocInterceptor;


import com.shabro.erp.consignor.activeMqProducer.Goods;
import com.shabro.erp.consignor.activeMqProducer.Producer;
import com.shabro.erp.consignor.common.SMS.SMS;
import com.shabro.erp.consignor.common.kit.Kit;
import com.shabro.erp.consignor.common.kit.MongoDBUtil;
import com.shabro.erp.consignor.common.kit.PushKit;
import com.shabro.erp.utils.BaseUtil;
import com.shabro.erp.vo.InvoiceVo;
import com.shabro.erp.vo.SubRequirementMessage;
import com.shabro.erp.vo.TaxRateVo;
import com.shabro.rpc.service.blockchain.BlockChainService;
import com.shabro.rpc.util.RpcResult;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author kui
 * @version 1.0 on 2017-9-4
 * @version 1.1 on 2017-9-5
 * @version 1.2 on 2017-9-9
 *
 */
@Before(IocInterceptor.class)
public class RequirementService {
	
	//private static final boolean ISDEV = false;//是否为开发测试环境
	private static final Logger logger = LoggerFactory.getLogger(RequirementService.class);
	private static final int MessageToal = 40; // 最大短信推送数目
	private static final double maxDistance = 150000.0; // 最大半径(m)

	//activeMQ 消息队列生产者
	private static Producer producer=Producer.me.init();
	//发布货源的消息名称
	private static String ERP_PUBLISH_GOODS= "erpPublishGoods";
	//是否需要异步发送信息
	private static Boolean ISASYNC = PropKit.getBoolean("async", false);

//	public static void main(String[] args) {
//		//2017-09-20 23:06:18
//		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		try {
//			System.out.println(format.format(format.parse("2017-09-20 23:06:18")));;
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}

	@Inject.BY_NAME
	private BlockChainService blockChainService;

	public void findById() {
		//System.out.println(""+cityOrderService);
		System.out.println(""+blockChainService);

		RpcResult<String> s = blockChainService.getName();
		System.out.println(""+s.getMessage());
	}

public boolean saveInvoiceRequirement(String reqId, String fbzId) {
	String sql = "SELECT * FROM ylgj_baseinfo_invoice  WHERE fbz_id='" + fbzId + "'";
	List<Record> list = Db.find(sql);
	if (list.size() > 0) {
		String insert_sql = "INSERT INTO  ssd_invoice_requirement(id,fbz_id,account_bank,account_num,duty_num,invoice_content,invoice_title,company_tel,company_address,consignee,consignee_address,consignee_phone,postage)" +
				"VALUES('" + reqId + "','" + fbzId + "','" + list.get(0).getStr("account_bank") + "','" + list.get(0).getStr("account_num") + "'" +
				",'" + list.get(0).getStr("duty_num") + "','" + list.get(0).getStr("invoice_content") + "','" + list.get(0).getStr("invoice_title") + "'" +
				",'" + list.get(0).getStr("company_tel") + "','" + list.get(0).getStr("company_address") + "','" + list.get(0).getStr("consignee") + "'" +
				",'" + list.get(0).getStr("consignee_address") + "','" + list.get(0).getStr("consignee_phone") + "'," + list.get(0).getBigDecimal("postage") + ")";

		return Db.update(insert_sql) > 0 ? true : false;
	} else {
		logger.info("没有查询到开票信息----" + fbzId);
		return false;
	}

}

	public Map<String, String> getFunctionVisible() {
		Map<String, String> map = new HashMap<String, String>();
		String sql = " select c.mkey, c.value, c.type from ylgj_sys_code c where c.type = 'visible' and c.value is not null";
		List<Record> data = Db.find(sql);
		for (Record o : data) {
			String key = o.get("mkey");
			String value = o.get("value");
			if (BaseUtil.stringNotNull(key) && BaseUtil.stringNotNull(value)) {
				map.put(key, value);
			}
		}
		if (map.isEmpty()) {
			map.put("state", "1");
			map.put("message", "获取失败");
		} else {
			map.put("state", "0");
			map.put("message", "获取成功");
		}
		return map;
	}

	public Map<String, Object> getPostage() {
		Map<String, Object> map = new HashMap<String, Object>();
		String sql = "SELECT value FROM ylgj_sys_code WHERE TYPE='invoice' AND mkey='postage'";
		List<Record> value = Db.find(sql);
		map.put("postage", value.get(0).getStr("value"));
		map.put("state", "0");
		map.put("message", "获取成功");
		return map;
	}

	public Map<String, String> saveOrUpdateInvoice(InvoiceVo invoiceVo) {
		Map<String, String> payload = new HashMap<String, String>();
		String sql = "SELECT * FROM ylgj_baseinfo_invoice  WHERE fbz_id='" + invoiceVo.getFbzId() + "'";
		List<Record> list = Db.find(sql);
		int result = 0;
		if (list.size() > 0) {
			String update_sql = "UPDATE ylgj_baseinfo_invoice SET invoice_title='" + invoiceVo.getInvoiceTitle() + "',invoice_content='" + invoiceVo.getInvoiceContent() + "'" +
					",account_num='" + invoiceVo.getAccountNum() + "',duty_num='" + invoiceVo.getDutyNum() + "',account_bank='" + invoiceVo.getAccountBank() + "'" +
					",consignee='" + invoiceVo.getConsignee() + "',consignee_phone='" + invoiceVo.getConsigneePhone() + "',consignee_address='" + invoiceVo.getConsigneeAddress() + "',company_address='" + invoiceVo.getCompanyAddress() + "',company_tel='" + invoiceVo.getCompanyTel() + "',postage=" + invoiceVo.getPostage() + "";
			result = Db.update(update_sql);

		} else {
			String insert_sql = "INSERT INTO ylgj_baseinfo_invoice(id,invoice_title,invoice_content,account_num,duty_num,account_bank,consignee,consignee_phone,consignee_address,company_address,company_tel,postage,fbz_id) \n" +
					"VALUES('" + UUID.randomUUID().toString() + "','" + invoiceVo.getInvoiceTitle() + "','" + invoiceVo.getInvoiceContent() + "','" + invoiceVo.getAccountNum() + "','" + invoiceVo.getDutyNum() + "','" + invoiceVo.getAccountBank() + "','" + invoiceVo.getConsignee() + "','" + invoiceVo.getConsigneePhone() + "'" +
					",'" + invoiceVo.getConsigneeAddress() + "','" + invoiceVo.getCompanyAddress() + "','" + invoiceVo.getCompanyTel() + "', " + invoiceVo.getPostage() + ",'" + invoiceVo.getFbzId() + "')";
			result = Db.update(insert_sql);

		}

		if (result > 0) {
			payload.put("state", "0");
			payload.put("message", "提交成功");
			return payload;
		} else {
			payload.put("state", "1");
			payload.put("message", "提交失败");
			return payload;
		}
	}


	public Map<String, Object> getInvoice(String fbzId) {
		Map<String, Object> map = new HashMap<String, Object>();
		String sql = "SELECT * FROM ylgj_baseinfo_invoice  WHERE fbz_id='" + fbzId + "'";
		List<Record> list = Db.find(sql);
		if (list.size() > 0) {
			//InvoiceVo invoiceVo = new InvoiceVo(list.get(0).getStr("id"), list.get(0).getStr("invoice_title"), list.get(0).getStr("invoice_content"), list.get(0).getStr("account_num"), list.get(0).getStr("duty_num"), list.get(0).getStr("account_bank"), list.get(0).getStr("consignee"), list.get(0).getStr("consignee_phone"), list.get(0).getStr("consignee_address"), list.get(0).getStr("fbz_id"), list.get(0).getBigDecimal("postage").intValue());
			//map.put("invoiceVo", invoiceVo);
			map.put("state", "0");
			map.put("message", "获取成功");
		} else {
			map.put("state", "1");
			map.put("message", "获取失败");
		}
		return map;
	}
	// 值乘以100
	private static String multiplyOneHundred(BigDecimal value) {
		BigDecimal base = new BigDecimal("100");
		base = base.multiply(value);
		return base.setScale(2, RoundingMode.HALF_EVEN).toString();
	}
	public Map<String, Object> getTaxRateList() {
		Map<String, Object> map = new HashMap<String, Object>();
		List<TaxRateVo> lstRax = new ArrayList<>();

		try {
			String sql = " select c.value, c.mkey, c.type from ylgj_sys_code c where c.type = 'taxRate' and c.value is not null order by id asc ";
			//List<Object[]> lst = commonService.getListSql(sql);
			List<Record> list = Db.find(sql);
			for (Record obj : list) {
				String rate = obj.get("value");
				String mkey = obj.get("mkey");
				String type = obj.get("type");

				BigDecimal taxRate = new BigDecimal(rate).setScale(4, RoundingMode.DOWN);
				int cmp = taxRate.compareTo(BigDecimal.ZERO);
				if (cmp > 0) {
					// 代司机开具*%物流增值税发票
					String num = multiplyOneHundred(taxRate).replaceAll("0*$", "");
					int idx = num.indexOf(".");
					int len = num.length();
					if (len == (idx + 1)) {
						num = num.substring(0, idx);
					}
					String desc = "代司机开具" + num + "%物流增值税发票";
					TaxRateVo vo = new TaxRateVo(taxRate, new Integer(1), desc);
					lstRax.add(vo);
				}
			}
			map.put("taxRateList", lstRax);
			map.put("state", "0");
			map.put("message", "获取成功");
		} catch (Exception e) {
			e.printStackTrace();
			map.put("state", "1");
			map.put("message", "获取失败");
		}
		return map;
	}

	/**
	 * 列出货主的货源
	 * @since 1.0
	 * @param fbzId 货主id
	 * @param pageNo 页码，从1开始
	 * @param pageSize 每页数量
	 * @param id   id
	 * @param isInsurance 是否有保险
	 * @param state   运单状态
	 * @param dateStart 时间开始
	 * @param dateEnd  时间结束
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> list(String fbzId,Integer pageNo, Integer pageSize, String id , Integer isInsurance, 
			Integer state, Date dateStart, Date dateEnd) {
		Map<String, Object> map;
		List<Object> lists = new ArrayList<>();
		String selectSql = "Select *";
		StringBuilder querySql = new StringBuilder("from view_erp_requirement_list as r where r.fbzId = '"+fbzId+"'");
		if(id != null) {
			//querySql.append(" and r.id like '"+id+"%'");
			querySql.append(" and r.id like '%"+id+"%'");
			//lists.add(id);
		}
		if(isInsurance != null) {
			//querySql.append(" and r.hasInsurance = "+isInsurance+"");
			querySql.append(" and r.hasInsurance = ?");
			lists.add(isInsurance);
		}
		if(state != null) {
			//querySql.append(" and r.state = "+state+"");
			querySql.append(" and r.state = ?");
			lists.add(state);
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//System.out.println(format.format(dateEnd));
		//UNIX_TIMESTAMP
		if(dateStart != null) {
			querySql.append(" and UNIX_TIMESTAMP(r.createDate) >= UNIX_TIMESTAMP('"+format.format(dateStart)+"')");
		}
		if(dateEnd != null) {
			querySql.append(" and UNIX_TIMESTAMP(r.createDate) <= UNIX_TIMESTAMP('"+format.format(dateEnd)+"')");
		}
		Page<Record> page = null;
		if(lists != null && lists.size() > 0) {
			page = Db.paginate(pageNo, pageSize, selectSql,querySql.toString(),lists.toArray());
		}else {
			page = Db.paginate(pageNo, pageSize, selectSql,querySql.toString());
		}
		
		map = Kit.toMap(page, "success", Kit.CODE_LOGIN_TRUE);
		return map;
		
	}
	/**
	 * 获取货源详细 和与货源对应的订单列表
	 * @since 1.0
	 * @param id
	 * @return
	 */
	public Map<String,Object> details(String id) {
		Map<String,Object> res = new HashMap<>();
		List<Record> list= Db.find("select * from view_erp_requirement_detail as r where r.id = ? ", id);
		if(list != null && list.size() > 0) {
			Record re = list.get(0);
			//对于车型不限的处理
			String temp = re.getStr("carType");
			if (temp == null || temp.trim().length() == 0 || temp.indexOf("不限") != -1 ) {
				re.set("carType", "不限");
			}
			res.put("requirement", list.get(0));
			List<Record> orders = Db.find("select * from view_erp_order_listByReqId where requirementId = ?",id);
			res.put("orders", orders);
			return res;
		}
		return res;
	}
	
	

	/**
	 * 获取地址
	 * @since 1.1
	 * @param fbzId
	 * @return
	 */
	public List<Record> getAddress(String fbzId,Integer type, String tel) {
		Map<String, Object> map = new HashMap<>();
		String sql1 = "select id,fbz_id as fbzId,tel as contactPhone, contact as contactName," + 
				"name as companyName," + 
				"lon as lon," + 
				"lat as lat," + 
				"address as address," + 
				"city," + 
				"district," + 
				"province," + 
				"address_detail as addressDetail" + 
				" from ssd_user_address_start where tel = ?";
				//" from ssd_user_address_start where fbz_id = ? and tel = ?";
		String sql2 = "select id,fbz_id as fbzId,tel as contactPhone, contact as contactName," + 
				"name as companyName," + 
				"lon as lon," + 
				"lat as lat," + 
				"address as address," + 
				"city," + 
				"district," + 
				"province," + 
				"address_detail as addressDetail" + 
				" from ssd_user_address_arrive where tel = ?";
				//" from ssd_user_address_arrive where fbz_id = ? and tel = ?";
		List<Record> res = null;
		if(type == 0) {
			res = Db.find(sql1, tel);
		}else {
		   res = Db.find(sql2, tel);
		}
		return res;
	}
	/**
	 * 获取常用货物类型名称
	 * @since 1.0
	 * @param fbzId
	 * @return
	 */
	public List<Record> getGoodsName(String fbzId){
		List<Record> list = Db.find("SELECT goods_type as typeName,id as id from ssd_goods_type where fbz_id = ? and goods_type is not null",fbzId);
		return getGoosType(list);
	}
	/**
	 * 删除用户自定义的货物类型
	 * @param fbzId
	 * @param id
	 * @param res
	 */
	public void delGoodsType(String fbzId, Integer id, Map<String, Object> res) {
		Db.update("delete from ssd_goods_type where id = "+id+" and fbz_id = '"+fbzId+"'");
		res.put("code", 200);
		res.put("msg", "success");
	}
	/**
	 * 添加货物类型，最多拥有四个
	 * @param fbzId
	 * @param goodsName
	 * @param res
	 */
	public void addGoodsType(String fbzId, String goodsName, Map<String, Object> res) {
		int i= Db.queryInt("select count(*) from ssd_goods_type where fbz_id = '"+fbzId+"'");
		if(i>=4) { //假如已经有四个了 删除最早添加的
			int id= Db.queryInt("select id from ssd_goods_type where fbz_id = '"+fbzId+"' ORDER BY create_time LIMIT 0,1");
			Db.update("delete from ssd_goods_type where id = "+id+"");
		}
		Db.update("INSERT INTO ssd_goods_type(fbz_id,goods_type,create_time)VALUES ('"+fbzId+"','"+goodsName+"',NOW())");
		res.put("code", 200);
		res.put("msg", "success");
	}
	/**
	 * 获取车辆类型
	 * @since 1.0
	 * @return
	 */
	public Map<String, Object> getCarInfo(){
		List<String> type = new ArrayList<>();
		List<String> lenght = new ArrayList<>();
		//SELECT value as name ,type as type from  ylgj_sys_code where type = 'carType' OR type = 'carLength'
		String sql ="select * from (SELECT value as name ,type as type from  ylgj_sys_code where type = 'carType' OR type = 'carLength') as tt where tt.name is not null";
		List<Record> list = Db.find(sql);
		if(list == null) {
			return null;
		}
		for(Record r:list) {
			if(r.getStr("type") != null && "carType".equals(r.getStr("type"))) {
				type.add(r.getStr("name"));
			}else {
				lenght.add(r.getStr("name"));
			}
			
		}
		Map<String, Object> map = new HashMap<>();
		map.put("type", type);
		map.put("lenght", lenght);
		return map;
	}
	
	/**
	 * 发布货源
	 * @since 1.1
	 * @param fbzId
	 * @param startAddressId
	 * @param arriveAddressId
	 * @param goodsName
	 * @param weight
	 * @param unit
	 * @param deliverTime
	 * @param arriveLimit
	 * @param carTypeName
	 * @param carLenght
	 * @param priceType
	 * @param needInvoice
	 * @param needInsurance
	 * @param scope
	 * @return
	 */
	public Integer publish(String fbzId, Integer startAddressId, Integer arriveAddressId, String goodsName, Double weight,
			Integer unit, Date deliverTime, Integer arriveLimit, String carTypeName, Double carLenght,Double maxCarLength,
			Integer priceType, Integer needInvoice, Integer needInsurance, Integer scope,
			Address startAddress, Address arriveAddress, Double price, Double dist, String areaCode, String arriveCode) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String sql1 = "select id,fbz_id as fbzId,tel as contactPhone, contact as contactName," + "name as companyName," + "lon as lon," + 
				"lat as lat," + "address as address," + "city," + "district," + "province," + "address_detail as addressDetail" + 
				" from ssd_user_address_start where id = ?";
		List<Record> start = Db.find(sql1, startAddressId);
		String sql2 = "select id,fbz_id as fbzId,tel as contactPhone, contact as contactName," + "name as companyName," + "lon as lon," + "lat as lat," + "address as address," +
				"city," + "district," + "province," + "address_detail as addressDetail" + 
				" from ssd_user_address_arrive where id = ?";
		List<Record> end = Db.find(sql2, arriveAddressId);
		Date now = new Date();
		Address a1  = startAddress;
		if(startAddressId == null || startAddressId <=0) {
			String sqlStart = "INSERT INTO ssd_user_address_start"
					+ "(fbz_id,tel,contact,name,lon,lat,address,city,district,address_detail,province,update_time) " + 
					"VALUES('"+fbzId+"','"+a1.getContactPhone()+"','"+a1.getContactName()+"',"
					+ "'',"+a1.getLon()+","+a1.getLat()+","
					+ "'"+a1.getAddress()+"','"+a1.getCity()+"',"
					+ "'"+a1.getDistrict()+"','"+a1.getAddressDetail()+"','"+a1.getProvince()+"','"+format.format(now)+"' )";
			try {//这里应该没有事物
				logger.info("\n【货源管理】-货源发布-保存收获地址---->执行sql:"+sqlStart+"");
				int i = Db.update(sqlStart);
				logger.info("\n【货源管理】-货源发布-保存收获地址---->【保存成功】\n 影响数据："+i+" 条");
			} catch (Exception e) {
				logger.info("\n【货源管理】-货源发布---->【保存收货地址出错，修改继续】");
				logger.error("\n【错误信息】");
//				e.printStackTrace();干掉这个错误信息 2017-11-23
			}
			
		} 
		Address a2 = arriveAddress; 
		if(arriveAddressId == null || arriveAddressId <=0) {
			String sqlArrive = "INSERT INTO ssd_user_address_arrive"
					+ "(fbz_id,tel,contact,name,lon,lat,address,city,district,address_detail,province,update_time) " + 
					"VALUES('"+fbzId+"','"+a2.getContactPhone()+"','"+a2.getContactName()+"',"
					+ "'',"+a2.getLon()+","+a2.getLat()+","
					+ "'"+a2.getAddress()+"','"+a2.getCity()+"',"
					+ "'"+a2.getDistrict()+"','"+a2.getAddressDetail()+"','"+a2.getProvince()+"','"+format.format(now)+"' )";
			try {//这里应该没有事物
				logger.info("\n【货源管理】-货源发布-保存卸货地址---->【执行sql】:"+sqlArrive+"");
				int i = Db.update(sqlArrive);
				logger.info("\n【货源管理】-货源发布-保存卸货地址---->【保存成功】\n 影响数据"+i+"");
			} catch (Exception e) {
				logger.info("\n【货源管理】-货源发布---->【保存卸货地址出错，修改继续】");
				logger.error("\n【错误信息】");
//				e.printStackTrace();干掉这个错误信息 2017-11-23
			}
		} 
		
		String reqId = generateID(now);
		//判断货源是否存在保证金
		Map<String, Object> map=checkGuaranteeByqid(reqId);
		if(map.get("data")!=null){
			return 2;
		}
		String sqlInsert = "insert into ylgj_order_requirement(id,start_address, "
				+ "start_province,start_district,start_address_detail,area_code,start_latitude,"
				+ "start_lon,start_lat,arrive_address,arrive_province,arrive_district,"
				+ "arrive_address_detail,arrive_code, arrive_latitude,arrive_lon,arrive_lat,deliver_time,"
				+ "arrive_limit,deliver_username,deliver_phone,arrive_username,arrive_phone,"
				+ "goods_name,weight,car_type,car_length,car_length_max,price_type,price,fbz_id,state,"
				+ "create_date,isNeedInvoice,reqType,insurance,push_code,publish_scope,"
				+ "finishWeight,confirmCyzCnt,bidCyzCnt,dist) values("
				+ "'"+reqId+"',"
				+"'"+a1.getCity()+"',"//
				+"'"+a1.getProvince()+"',"
				+"'"+a1.getDistrict()+"',"
				+"'"+a1.getAddressDetail()+"',"
				+""+quoteString(areaCode)+","
				+"'("+a1.getLon()+","+a1.getLat()+")',"
				+""+a1.getLon()+","
				+""+a1.getLat()+","
				+"'"+a2.getCity()+"',"
				+"'"+a2.getProvince()+"',"
				+"'"+a2.getDistrict()+"',"
				+"'"+a2.getAddressDetail()+"',"
				+""+quoteString(arriveCode)+","
				+"'("+a2.getLon()+","+a2.getLat()+")',"
				+""+a2.getLon()+","
				+""+a2.getLat()+","
				+"'"+format.format(deliverTime)+"',"
				+""+arriveLimit+","
				+"'"+a1.getContactName()+"',"
				+"'"+a1.getContactPhone()+"',"
				+"'"+a2.getContactName()+"',"
				+"'"+a2.getContactPhone()+"',"
				+"'"+goodsName+"',"
				+""+weight+","
				+""+quoteString(carTypeName)+","
				+""+carLenght+","
				+ ""+maxCarLength+","
				+""+priceType+","
				+""+price+","//price
				+"'"+fbzId+"',"
				+"0,"
				+"'"+format.format(now)+"',"
				+""+needInvoice+","
				+""+unit+","
				+"'"+needInsurance+"',"
				+"'"+generatePublishCode()+"',"
				+""+scope+","
				+ "0,0,0,"+dist+")"; 
		logger.info("\n【货源管理】-货源发布----->【执行sql】\n--"+sqlInsert);
	    int i=	Db.update(sqlInsert);
	    logger.info("\n【货源管理】-货源发布----->【发布成功】\n 受影响数据："+i+" 条");


		sqlInsert = "insert into ssd_requirement_addition (id,create_date) values("
					+ "'"+reqId+"',"
					+ "'"+format.format(now)+"')";
		i = Db.update(sqlInsert);
		logger.info("\n【货源管理】-货源发布----->插入货源附加信息记录result="+i);

	    try {//这里应该没有事物
			if (ISASYNC) {//异步发送短信和极光推送
				Goods goods = new Goods(1, a1.getLon(), a1.getLat(), fbzId, scope, goodsName, carLenght, maxCarLength, carTypeName, priceType, price, unit, weight, a1, a2, deliverTime, reqId);
				producer.sendMessage(ERP_PUBLISH_GOODS, goods);
			}else {//同步发送短信和极光推送
				pushMessage(1, a1.getLon(), a1.getLat(), fbzId, scope, goodsName, carLenght, maxCarLength, carTypeName, priceType, price, unit, weight, a1, a2, deliverTime, reqId);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("\n【货源管理】-货源发布-消息推送---->【推送出错】");
		}
		/**
		 * ==================在此添加区块链的数据更新代码=======================
		 */
		if(i==1) {
			RpcResult<String> rpcResult = null;
			SubRequirementMessage subRequirementMessage = null;
			try {
				subRequirementMessage = new SubRequirementMessage();
				subRequirementMessage.setId( reqId );
				subRequirementMessage.setStartAddress( a1.getCity() );
				subRequirementMessage.setStartProvince( a1.getProvince() );
				subRequirementMessage.setStartDistrict( a1.getDistrict() );
				subRequirementMessage.setStartAddressDetail( a1.getAddressDetail() );
				subRequirementMessage.setStartLatitude( "(" + a1.getLon() + "," + a1.getLat() + ")" );
				subRequirementMessage.setArriveAddress( a2.getCity() );
				subRequirementMessage.setArriveProvince( a2.getProvince() );
				subRequirementMessage.setArriveDistrict( a2.getDistrict() );
				subRequirementMessage.setArriveAddressDetail( a2.getAddressDetail() );
				subRequirementMessage.setArriveLatitude( "(" + a2.getLon() + "," + a2.getLat() + ")" );
				subRequirementMessage.setDeliverUsername( a1.getContactName() );
				subRequirementMessage.setArriveUsername( a2.getContactName() );
				subRequirementMessage.setGoodsName( goodsName );
				subRequirementMessage.setCreateDate( format.format( new Date() ) );
				subRequirementMessage.setWeight( String.valueOf( weight ) );
				subRequirementMessage.setCarType( quoteString( carTypeName ) );
				subRequirementMessage.setAreaCode( areaCode );
				subRequirementMessage.setArriveCode( arriveCode );
				subRequirementMessage.setPrice( String.valueOf( price ) );
				subRequirementMessage.setPriceType( String.valueOf( priceType ) );
				subRequirementMessage.setDist( String.valueOf( dist ) );
				subRequirementMessage.setReqType( String.valueOf( unit ) );
				subRequirementMessage.setState( "0" );
				JSONObject jsonObject = JSONObject.fromObject( subRequirementMessage );
				String json = jsonObject.toString();
				rpcResult = blockChainService.updateReqInfo( subRequirementMessage.getId(), json );

			} catch (Exception e) {
				logger.error( "修改区块链失败" );
				logger.error( rpcResult.toString() );
			}
			logger.info( "修改区块链成功" );
			logger.info( rpcResult.toString() );
		}
		/**
		 * ==================在此添加区块链的数据更新代码=======================
		 */
	    return i;
		
	}
	
	/**
	 * 更新
	 * @param id
	 * @param fbzId
	 * @param startAddressId
	 * @param arriveAddressId
	 * @param goodsName
	 * @param weight
	 * @param unit
	 * @param deliverTime
	 * @param arriveLimit
	 * @param carTypeName
	 * @param carLenght
	 * @param priceType
	 * @param needInvoice
	 * @param needInsurance
	 * @param scope
	 * @param startAddress
	 * @param arriveAddress
	 * @param price
	 * @return
	 */
	public Integer update(String id, String fbzId, Integer startAddressId, Integer arriveAddressId, String goodsName, Double weight,
			Integer unit, Date deliverTime, Integer arriveLimit, String carTypeName, Double carLenght,Double maxCarLength,
			Integer priceType, Integer needInvoice, Integer needInsurance, Integer scope,
			Address startAddress, Address arriveAddress, Double price, Double dist, String areaCode, String arriveCode) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String sql1 = "select id,fbz_id as fbzId,tel as contactPhone, contact as contactName," + "name as companyName," + "lon as lon," + 
				"lat as lat," + "address as address," + "city," + "district," + "province," + "address_detail as addressDetail" + 
				" from ssd_user_address_start where id = ?";
		List<Record> start = Db.find(sql1, startAddressId);
		String sql2 = "select id,fbz_id as fbzId,tel as contactPhone, contact as contactName," + "name as companyName," + "lon as lon," + "lat as lat," + "address as address," +
				"city," + "district," + "province," + "address_detail as addressDetail" + 
				" from ssd_user_address_arrive where id = ?";
		List<Record> end = Db.find(sql2, arriveAddressId);

		Address a1  = startAddress;
		if(startAddressId == null || startAddressId <=0) {
			String sqlStart = "INSERT INTO ssd_user_address_start"
					+ "(fbz_id,tel,contact,name,lon,lat,address,city,district,address_detail,province,update_time) " + 
					"VALUES('"+fbzId+"','"+a1.getContactPhone()+"','"+a1.getContactName()+"',"
					+ "'',"+a1.getLon()+","+a1.getLat()+","
					+ "'"+a1.getAddress()+"','"+a1.getCity()+"',"
					+ "'"+a1.getDistrict()+"','"+a1.getAddressDetail()+"','"+a1.getProvince()+"','"+format.format(new Date())+"' )";
			try {
				logger.info("\n【货源管理】-货源修改-保存装货地址---->【执行sql】:"+sqlStart+"");
				int i = Db.update(sqlStart);
				logger.info("\n【货源管理】-货源修改-保存装货地址---->【保存成功】\n 影响数据："+i+" 条");
			} catch (Exception e) {//这里存储地址信息，无需处理异常，无需事物
//				e.printStackTrace();
				 logger.info("\n【货源管理】-货源修改-保存装货地址---->【失败，修改继续】");
				 logger.error("\n【错误信息】");
				
			}
			
		} 
		Address a2 = arriveAddress; 
		if(arriveAddressId == null || arriveAddressId <=0) {
			String sqlArrive = "INSERT INTO ssd_user_address_arrive"
					+ "(fbz_id,tel,contact,name,lon,lat,address,city,district,address_detail,province,update_time) " + 
					"VALUES('"+fbzId+"','"+a2.getContactPhone()+"','"+a2.getContactName()+"',"
					+ "'',"+a2.getLon()+","+a2.getLat()+","
					+ "'"+a2.getAddress()+"','"+a2.getCity()+"',"
					+ "'"+a2.getDistrict()+"','"+a2.getAddressDetail()+"','"+a2.getProvince()+"','"+format.format(new Date())+"' )";
			try {
				logger.info("\n【货源管理】-货源修改-保存卸货地址---->【执行sql】:"+sqlArrive+"");
				int i = Db.update(sqlArrive);
				logger.info("\n【货源管理】-货源修改-保存卸货地址---->【保存成功】\n 影响数据："+i+" 条");
			} catch (Exception e) {//这里存储地址信息，无需处理异常，无需事物
//				e.printStackTrace();
				 logger.info("\n【货源管理】-货源修改-保存卸货地址----->【卸货地址出错，修改继续】");
				 logger.error("\n【错误信息】");
			}
			
		}

		//判断货源是否存在保证金
		Map<String, Object> map=checkGuaranteeByqid(id);
		if(map.get("data")!=null){
			return 2;
		}

		String updateSql = "update ylgj_order_requirement set "
				+" start_address='"+a1.getCity()+"',"
				+"start_province='"+a1.getProvince()+"',"
				+"start_district='"+a1.getDistrict()+"',"
				+"start_address_detail='"+a1.getAddressDetail()+"',"
				+"start_latitude='("+a1.getLon()+","+a1.getLat()+")',"
				+"start_lon="+a1.getLon()+","
				+"start_lat="+a1.getLat()+","
				+"arrive_address='"+a2.getCity()+"',"
				+"arrive_province='"+a2.getProvince()+"',"
				+"arrive_district='"+a2.getDistrict()+"',"
				+"arrive_address_detail='"+a2.getAddressDetail()+"',"
				+"arrive_latitude='("+a2.getLon()+","+a2.getLat()+")',"
				+"arrive_lon="+a2.getLon()+","
				+"arrive_lat="+a2.getLat()+","
				+"deliver_time='"+format.format(deliverTime)+"',"
				+"arrive_limit="+arriveLimit+","
				+"deliver_username='"+a1.getContactName()+"',"
				+"deliver_phone='"+a1.getContactPhone()+"',"
				+"arrive_username='"+a2.getContactName()+"',"
				+"arrive_phone='"+a2.getContactPhone()+"',"
				+"goods_name='"+goodsName+"',"
				+"weight="+weight+","
				+"car_type="+quoteString(carTypeName)+","
				+"car_length="+carLenght+","
				+"car_length_max = "+maxCarLength+","
				+"price_type="+priceType+","
				+"price="+price+","//price
				+"fbz_id='"+fbzId+"',"
				+"state=0,"
				+"create_date='"+format.format(new Date())+"',"
				+"isNeedInvoice="+needInvoice+","
				+"reqType="+unit+","
				+"insurance='"+needInsurance+"',"
				+"push_code='"+generatePublishCode()+"',"
				+"publish_scope="+scope+","
				+ "dist = "+dist+"";

		String codeStart  = quoteString(areaCode);
		String codeArrive = quoteString(arriveCode);
		if (!"null".equals(codeStart)) {
			updateSql += ", area_code=" +codeStart;
		}
		if (!"null".equals(codeArrive )) {
			updateSql += ", arrive_code=" +codeArrive;
		}
		updateSql += " where id = '"+id+"' and state = 0";

		logger.info("\n 【货源管理】-货源修改---->【执行sql】\n -"+updateSql+"");
	    int i=	Db.update(updateSql);

		try {//这里应该没有事物
		if (ISASYNC) {//异步激光推送
			Goods goods = new Goods(0, a1.getLon(), a1.getLat(), fbzId, scope, goodsName, carLenght, maxCarLength,carTypeName, priceType, price, unit, weight, a1, a2, deliverTime, id);
			producer.sendMessage(ERP_PUBLISH_GOODS, goods);
		}else {//同步激光推送
			pushMessage(0, a1.getLon(), a1.getLat(), fbzId, scope, goodsName, carLenght, maxCarLength, carTypeName, priceType, price, unit, weight, a1, a2, deliverTime, id);
		}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("\n【货源管理】-货源发布-消息推送---->【推送出错】");
		}
	    logger.info("\n 【货源管理】-货源修改---->【修改成功】\n 货源id："+id+"\n 货源表受影响："+i+" 条");
		/**
		 * ==================在此添加区块链的数据更新代码=======================
		 */
		if (i==1){
			RpcResult<String>  rpcResult=null;
			SubRequirementMessage subRequirementMessage=null;
			try{
				subRequirementMessage=new SubRequirementMessage(  );
				subRequirementMessage.setId(id);
				subRequirementMessage.setStartAddress(a1.getCity());
				subRequirementMessage.setStartProvince(a1.getProvince());
				subRequirementMessage.setStartDistrict(a1.getDistrict());
				subRequirementMessage.setStartAddressDetail(a1.getAddressDetail());
				subRequirementMessage.setStartLatitude("("+a1.getLon()+","+a1.getLat()+")");
				subRequirementMessage.setArriveAddress(a2.getCity());
				subRequirementMessage.setArriveProvince(a2.getProvince());
				subRequirementMessage.setArriveDistrict(a2.getDistrict());
				subRequirementMessage.setArriveAddressDetail(a2.getAddressDetail());
				subRequirementMessage.setArriveLatitude("("+a2.getLon()+","+a2.getLat()+")");
				subRequirementMessage.setDeliverUsername(a1.getContactName());
				subRequirementMessage.setArriveUsername(a2.getContactName());
				subRequirementMessage.setGoodsName(goodsName);
				subRequirementMessage.setCreateDate(format.format(new Date()));
				subRequirementMessage.setWeight(String.valueOf(weight));
				subRequirementMessage.setCarType(quoteString(carTypeName));
				subRequirementMessage.setAreaCode(areaCode);
				subRequirementMessage.setArriveCode(arriveCode);
				subRequirementMessage.setPrice(String.valueOf(price));
				subRequirementMessage.setPriceType(String.valueOf(priceType));
				subRequirementMessage.setDist(String.valueOf(dist));
				subRequirementMessage.setReqType(String.valueOf(unit));
				subRequirementMessage.setState("0") ;
				JSONObject jsonObject=JSONObject.fromObject( subRequirementMessage );
				String json=jsonObject.toString();
				rpcResult=blockChainService.updateReqInfo(subRequirementMessage.getId(),json);
			}catch (Exception e){
				logger.error( "修改区块链失败" );
				logger.error(rpcResult.toString());
			}
			logger.info( "修改区块链成功" );
			logger.info(rpcResult.toString());
		}
		/**
         * ==================在此添加区块链的数据更新代码=======================
         */

	    return i;
		
	}
	/**
	 * 获取竞价司机列表
	 * @since 1.1
	 * @param fbzId
	 //* @param orderId
	 * @return
	 */
	public Map<String, Object> listCompeteCyz(String fbzId, String requirementId, Integer pageNo, Integer pageSize) {
		
		//获取货源信息
		String sqlR = "SELECT \r\n" + 
				"r.createDate,r.deliverTime as deliverDate,r.goodsName,r.id as requirementNo,r.unit,r.weight," + 
				"DATE_ADD(r.createDate,INTERVAL r.arriveLimit day ) as arriveDate," + 
				"CONCAT_WS('-->',r.deliverCity,r.arriveCity) as line," + 
				"r.carType," + 
				"r.carLength " + 
				"from view_erp_requirement_detail as r where id = '"+requirementId+"'"; 
		Record requirement = Db.findFirst(sqlR);
		String sqlSelect = "select *  ";
		String sql = "from view_erp_requirement_listCompete where fbzId = ? and requirementId = ?";
		Page<Record> page = Db.paginate(pageNo, pageSize, sqlSelect,sql,fbzId,requirementId);
		Map<String, Object>  res = new HashMap<>();
		res.put("requirement", requirement);
		res.put("page", page);
		return res;
	}
	
	/**
	 * 选择承运
	 * @since 1.1
	 * @param orderId
	 */
	public Map<String, Object> choose(String orderId, String reqId) {
		 Map<String, Object> res = new HashMap<>();
		//判断货物书否允许被选择司机
		String sql = "select *  from view_erp_requirement_list as r where r.id = ? and state in (0,1)";
		List<Record> requirements = Db.find(sql,reqId);
		if(requirements == null || requirements.size() == 0) {
			res.put("data", null);
			res.put("code", 200);
			res.put("msg", "货物被接单停止或已经完成运输,自动把未选中的订单设置为‘未选中’状态");
			int i = Db.update("update ylgj_order_bid set order_state = 6 where requirement_id = '"+reqId+"' and order_state = 0");
			logger.info("\n 【货源管理】-选择承运----->【操所失败】：没有查询到货源信息,自动把未选中的订单设置为‘未选中’状态!\n 货源id："+reqId+" \n 受影响的数据："+i+" 条");
			return res;
		}
		//判断货源是否存在保证金
		Map<String, Object> map=checkGuaranteeByoid(orderId);
		Object obj=map.get("data");
		if(obj!=null){
			res.put("data", null);
			res.put("code", 200);
			res.put("msg", "该货源存在保证金，暂时无法选择承运，请在运力管家APP进行操作!");
			return res;
		}
		//判断剩余数量
		//SELECT CASE WHEN  ISNULL(r.finishWeight) THEN r.weight ELSE r.weight-r.finishWeight end as 'surplus'  from ylgj_order_requirement as r
		String sql2 = "SELECT CASE WHEN  ISNULL(r.finishWeight) THEN r.weight ELSE r.weight-r.finishWeight end as 'surplus'  from ylgj_order_requirement as r where id = ?";
		
		String sql3 = "select bid_weight as w  from ylgj_order_bid where id = ?";
		Double bidWeight = Db.find(sql3, orderId).get(0).getDouble("w");//接单数量
		Double surplus = Db.find(sql2, reqId).get(0).getDouble("surplus");//货源剩余可接单的数量
		
		if (surplus <= 0) {//当货源重量为0时 ,把订单改为未中选，货源改为已完成
			res.put("data", null);
			res.put("code", 200);
			res.put("msg", "货物已经完成运输");
			int i = Db.update("update ylgj_order_bid set order_state = 5 where requirement_id = '"+reqId+"' and order_state = 0");
			int j = Db.update("update ylgj_order_requirement set state = 2 where id = '"+reqId+"'");
			logger.info("\n 【货源管理】-选择承运----->【操所失败】：货源已无剩余可接单重量,自动把未选中的订单设置为‘未选中’状态,自动将货源修改为‘完成’状态!\n 货源id："+reqId+"\n 受影响的数据："+(i + j)+" 条");
			/**
			 *=====================拟插入位置区块链=====================================
			 */
			if (j==1) {
				RpcResult<String> rpcResult = null;
				try {
					Record record = Db.findById( "ylgj_order_requirement", reqId );
					Map<String, Object> colmap = record.getColumns();
					colmap.put( "state", 2 );
					Map<String, Object> jsonMap = new HashMap<>();
					jsonMap.put( "id", colmap.get( "id" ) );
					jsonMap.put( "startAddress", colmap.get( "start_address" ) );
					jsonMap.put( "startProvince", colmap.get( "start_province" ) );
					jsonMap.put( "startDistrict", colmap.get( "start_district" ) );
					jsonMap.put( "startAddressDetail", colmap.get( "start_address_detail" ) );
					jsonMap.put( "startLatitude", colmap.get( "start_latitude" ) );
					jsonMap.put( "arriveAddress", colmap.get( "arrive_address" ) );
					jsonMap.put( "arriveProvince", colmap.get( "arrive_province" ) );
					jsonMap.put( "arriveDistrict", colmap.get( "arrive_district" ) );
					jsonMap.put( "arriveAddressDetail", colmap.get( "arrive_address_detail" ) );
					jsonMap.put( "arriveLatitude", colmap.get( "arrive_latitude" ) );
					jsonMap.put( "deliverUsername", colmap.get( "deliver_username" ) );
					jsonMap.put( "arriveUsername", colmap.get( "arrive_username" ) );
					jsonMap.put( "goodsName", colmap.get( "goods_name" ) );
					jsonMap.put( "createDate", colmap.get( "create_date" ) );
					jsonMap.put( "weight", colmap.get( "weight" ) );
					jsonMap.put( "carType", colmap.get( "car_type" ) );
					jsonMap.put( "areaCode", colmap.get( "area_code" ) );
					jsonMap.put( "arriveCode", colmap.get( "arrive_code" ) );
					jsonMap.put( "price", colmap.get( "price" ) );
					jsonMap.put( "priceType", colmap.get( "price_type" ) );
					jsonMap.put( "dist", colmap.get( "dist" ) );
					jsonMap.put( "reqType", colmap.get( "reqType" ) );
					jsonMap.put( "state", colmap.get( "state" ) );
					String json = com.alibaba.fastjson.JSON.toJSONString( jsonMap, true );
					rpcResult = blockChainService.updateReqInfo( String.valueOf( jsonMap.get( "id" ) ), json );
				} catch (Exception e) {

					logger.error( "区块链插入失败" );
					logger.error( rpcResult.toString() );
				}
				logger.info( "区块链插入成功" );
				logger.info( rpcResult.toString() );
			}
			/**
			 *=================================拟插入位置区块链=====================================
			 */

			return res;
		}
		if (surplus - bidWeight < 0) {//假如接单数量多余剩余数量，则把该订单作废
			int i = Db.update("update ylgj_order_bid set order_state = 5 where id = '"+orderId+"' and order_state = 0");
			res.put("data", null);
			res.put("code", 200);
			res.put("msg", "接单数量大于剩余数量，该订单已被自动设置为未选中状态！");
			logger.info("\n 【货源管理】-选择承运----->【操所失败】：接单数量大于剩余数量，该订单已被自动设置为未选中状态！!\n 货源id："+reqId+"\n 订单号："+orderId+" \n 受影响的数据："+i+" 条");
			return res;
		}

		//更新订单
		int i = Db.update("update ylgj_order_bid set order_state = 1, bid_time = ? where id = '"+orderId+"' and order_state = 0", new Date());
		if (i < 1) {
			res.put("data", null);
			res.put("code", 200);
			res.put("msg", "订单状态错误！");
			return res;
		}

		//更新货源
		int j = Db.update("update ylgj_order_requirement SET finishWeight = CASE WHEN ISNULL(finishWeight) THEN "+bidWeight+" ELSE finishWeight+"+bidWeight+" end,"
				+ "confirmCyzCnt = CASE WHEN ISNULL(confirmCyzCnt) THEN 1 ELSE confirmCyzCnt+1 END where id = ? ",reqId);
		
		res.put("data", null);
		res.put("code", 200);
		res.put("msg", "选择成功，请尽快支付");
		logger.info("\n 【货源管理】-选择承运----->【操作成功】\n 货源id: "+reqId+"\n 订单号:"+orderId+"\n 货源表受影响数据："+j+" 条");
		return res;
		
	}
	
	/**
	 * 拒绝
	 * @since 1.1
	 * @param orderId
	 * @return
	 */
	public Map<String, Object> refuse(String orderId) {
		 Map<String, Object> res = new HashMap<>();
		int i= Db.update("update ylgj_order_bid set order_state = 5 where id = '"+orderId+"' and order_state = 0");
		logger.info("\n 【货源管理】-拒绝承运----->【操作成功】\n  订单号:"+orderId+"\n  订单表中受影响数据："+i+" 条");
		if(i == 1) {
			res.put("data", null);
			res.put("code", 200);
			res.put("msg", "拒绝成功");
		} else {
			res.put("data", null);
			res.put("code", 2);
			res.put("msg", "操作没有成功");
		}
		return res;
	}
	/**
	 * 停止
	 * @since 1.1
	 * @param reqId
	 * @return
	 */
	public Map<String, Object> stop(String reqId) {
		Map<String, Object> res = new HashMap<>();
		//判断货源是否存在保证金
		Map<String, Object> map=checkGuaranteeByqid(reqId);
		Object obj=map.get("data");
		if(obj!=null){
			res.put("data", null);
			res.put("code", 3);
			res.put("msg", "该货源存在保证金，暂时无法取消货源，请在运力管家APP进行操作!");
			return res;
		}else {
			int i = Db.update("update ylgj_order_requirement set state = 3 where id = ? and state in (0,1)", reqId);

			if (i == 1) {
				int j = Db.update("UPDATE ylgj_order_bid set order_state = 6 WHERE requirement_id = ? AND order_state = 0 ", reqId);
				logger.info("【停止竞标】-货源Id为:" + reqId + "----->停止竞标成功，受影响的订单体条数为：" + j);
				res.put("data", null);
				res.put("code", 200);
				res.put("msg", "操作成功");
				/**
				 *=====================拟插入位置区块链=====================================
				 */
				RpcResult<String> rpcResult=null;
				try{
					Record record=Db.findById( "ylgj_order_requirement",reqId );
					Map<String,Object> colmap=record.getColumns();
					colmap.put( "state",3 );
					Map<String,Object> jsonMap=new HashMap<>(  );
					jsonMap.put( "id",colmap.get( "id" ) );
					jsonMap.put( "startAddress",colmap.get( "start_address" ) );
					jsonMap.put( "startProvince",colmap.get( "start_province" ) );
					jsonMap.put( "startDistrict",colmap.get( "start_district" ) );
					jsonMap.put( "startAddressDetail",colmap.get( "start_address_detail" ) );
					jsonMap.put( "startLatitude",colmap.get( "start_latitude" ) );
					jsonMap.put( "arriveAddress",colmap.get( "arrive_address" ) );
					jsonMap.put( "arriveProvince",colmap.get( "arrive_province" ) );
					jsonMap.put( "arriveDistrict",colmap.get( "arrive_district" ) );
					jsonMap.put( "arriveAddressDetail",colmap.get( "arrive_address_detail" ) );
					jsonMap.put( "arriveLatitude",colmap.get( "arrive_latitude" ) );
					jsonMap.put( "deliverUsername",colmap.get( "deliver_username" ) );
					jsonMap.put( "arriveUsername",colmap.get( "arrive_username" ) );
					jsonMap.put( "goodsName",colmap.get( "goods_name" ) );
					jsonMap.put( "createDate",colmap.get( "create_date" ) );
					jsonMap.put( "weight",colmap.get( "weight" ) );
					jsonMap.put( "carType",colmap.get( "car_type" ) );
					jsonMap.put( "areaCode",colmap.get( "area_code" ) );
					jsonMap.put( "arriveCode",colmap.get( "arrive_code" ) );
					jsonMap.put( "price",colmap.get( "price" ) );
					jsonMap.put( "priceType",colmap.get( "price_type" ) );
					jsonMap.put( "dist",colmap.get( "dist" ) );
					jsonMap.put( "reqType",colmap.get( "reqType" ) );
					jsonMap.put( "state",colmap.get( "state" ) );
					String json=com.alibaba.fastjson.JSON.toJSONString( jsonMap,true );
					rpcResult=blockChainService.updateReqInfo(String.valueOf( jsonMap.get( "id" ) ),json);
				}catch (Exception e){

					logger.error( "区块链插入失败" );
					logger.error(rpcResult.toString());
				}
				logger.info( "区块链插入成功" );
				logger.info(rpcResult.toString());
				/**
				 *=================================拟插入位置区块链=====================================
				 */

			} else {
				res.put("data", null);
				res.put("code", 2);
				res.put("msg", "操作没有成功");
			}
			return res;
		}
	}
	/**
	 * 判断是否修改货源信息
	 * @param fbzId
	 * @param requirementId
	 * @param res
	 * @since 1.2
	 */
	public void canEditRequirement(String fbzId, String requirementId ,Map<String,Object> res) {
		String sql = "SELECT erp_canEditRequirement('"+fbzId+"','"+requirementId+"')";
		int i = Db.queryInt(sql);
		if(i == 1) {
			res.put("data", true);
		}else {
			res.put("data", false);
		}
		res.put("code", 200);
		res.put("msg","success");
	}
	
	
	/**
	 * 生成货源id
	 * @since 1.1
	 * @return
	 */
	private static String generateID(Date nowTime) {
		//生成需求ID
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		Random r = new Random();
		int a =r.nextInt(9999)%(9000)+1000;
		String now = df.format(nowTime.getTime());
		//R＋年月日时分秒＋4位随机数
		String requirementId="R"+now+a;
		return requirementId;
	}
		
	
	/**
	 * 生成推送码
	 * @since 1.1
	 * @return
	 */
	private static String generatePublishCode() {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		String now = df.format(System.currentTimeMillis());
		Random r = new Random();
		int a =r.nextInt(99999)%(90000)+10000;
		String code = now.substring(6,8) + a;
		return code;
	}
	
	/**
	 * @since 1.1
	 * @param list
	 * @return
	 */
	private static Address getAddress(List<Record> list) {
		Address a = new Address();
		for(Record r:list) {
			a.setContactPhone(r.getStr("contactPhone"));
			a.setContactName(r.getStr("contactName"));
			a.setLon(r.getDouble("lon"));
			a.setLat(r.getDouble("lat"));
			a.setAddress(r.getStr("address"));
			a.setCity(r.getStr("city"));
			a.setDistrict(r.getStr("district"));
			a.setProvince(r.getStr("province"));
			a.setAddressDetail(r.getStr("addressDetail"));
		}
		return a;
	}
	/**
	 * @since 1.1
	 * @author kui
	 */
	public static class Address implements Serializable{
		private static final long serialVersionUID = -643284384282602915L;
		private String contactPhone;
		private String  contactName;
		private Double lon;
		private Double lat;
		private String address;
		private String city;
		private String district;
		private String province;
		private String addressDetail;
		
		
		
		public Address(String contactPhone, String contactName, Double lon, Double lat, String address, String city,
				String district, String province, String addressDetail ) {
			super();
			this.contactPhone = contactPhone;
			this.contactName = contactName;
			this.lon = lon;
			this.lat = lat;
			this.address = address;
			this.city = city;
			this.district = district;
			this.province = province;
			this.addressDetail = addressDetail;
		}
		public Address() {
			super();
		}
		public String getContactPhone() {
			return contactPhone;
		}
		public void setContactPhone(String contactPhone) {
			this.contactPhone = contactPhone;
		}
		public String getContactName() {
			return contactName;
		}
		public void setContactName(String contactName) {
			this.contactName = contactName;
		}
		public Double getLon() {
			return lon;
		}
		public void setLon(Double lon) {
			this.lon = lon;
		}
		public Double getLat() {
			return lat;
		}
		public void setLat(Double lat) {
			this.lat = lat;
		}
		public String getAddress() {
			return address;
		}
		public void setAddress(String address) {
			this.address = address;
		}
		public String getCity() {
			return city;
		}
		public void setCity(String city) {
			this.city = city;
		}
		public String getDistrict() {
			return district;
		}
		public void setDistrict(String district) {
			this.district = district;
		}
		public String getProvince() {
			return province;
		}
		public void setProvince(String province) {
			this.province = province;
		}
		public String getAddressDetail() {
			return addressDetail;
		}
		public void setAddressDetail(String addressDetail) {
			this.addressDetail = addressDetail;
		}
	}
	
	private static List<Record> getGoosType(List<Record> list) {
		List<Record> res = new ArrayList<>();
		String [] types = {"普货","重货","泡货","设备","配件","百货","建材","食品","饮料","化工",
		        "水果","蔬菜","木材","煤炭","石材","家具","树苗","化肥","粮食","钢材","快递"};
		for(String t:types) {
			Record r = new Record();
			r.set("goodsName", t);
			r.set("id", 0);
			r.set("type", 0);
			res.add(r);
		}
		if(list != null && list.size() > 0) {
			for(Record r : list) {
				if(r.get("typeName") != null) {
					Record rr = new Record();
					rr.set("goodsName", r.get("typeName").toString());
					rr.set("id", r.getInt("id"));
					rr.set("type", 1);
					res.add(rr);
				}
			}
		}
		return res;
	}
	
	public static class goodsType implements Serializable{
		private Integer id;
		private String goodsName;
		private Integer type;
		public Integer getId() {
			return id;
		}
		public void setId(Integer id) {
			this.id = id;
		}
		public String getGoodsName() {
			return goodsName;
		}
		public void setGoodsName(String goodsName) {
			this.goodsName = goodsName;
		}
		public Integer getType() {
			return type;
		}
		public void setType(Integer type) {
			this.type = type;
		}
		
	}
	/**
	 * 货源推送消息
	 * @param lon
	 * @param lat
	 * @param fbzId
	 * @param scope
	 * @param goodsName
	 * @param carLenght
	 * @param maxCarLength
	 * @param carType
	 * @param priceType
	 * @param price
	 * @param unit
	 * @param weight
	 */
	//发送短信的功能已经分离到一个单独的系统--ssd-async  2018.3.26 by cat
	public static void pushMessage(int publish, Double lon, Double lat, String fbzId, Integer scope,
			String goodsName, Double carLenght, Double maxCarLength, String carType, Integer priceType, 
			Double price ,Integer unit, Double weight,Address a1 ,Address a2,Date deliverTime ,String reqId) {
		if(scope == 1) {//若发布到我的车队
			logger.info("\n【货源管理】-货源通知-极光推送---->"
					+ "从数据库中获取车队成员   【推送继续！】");
			String sql = "SELECT c.cyz_id as cyzId,c.cyz_tel as cyzTel from ylgj_user_convey as c  where c.fbz_id = '"+fbzId+"'";
			List<Record> records = Db.find(sql);
			if(records != null && records.size() > 0) {
				int len = records.size();
				List<String> tels = new ArrayList<>();
				for (int i = 0; i < len ; i++) {
					tels.add(records.get(i).getStr("cyzTel"));
				}
				String fbzName = Db.queryStr("SELECT fbz.`name` as fbzName from ylgj_user_fbz  as fbz where fbz.id = '"+fbzId+"'");
				String alertContent = String.format("%s对您发了一条货运信息，请及时查看！", fbzName);
				PushKit.hcdhPushMany(tels, alertContent, "", 1+"", "");
				logger.info("\n【货源管理】-货源通知-极光推送---->"
						+ "【推送成功！】");
			} else {
				logger.info("\n【货源管理】-货源通知-极光推送---->"
						+ "车队空空也   【推送结束！】");
			}
		} else {//发布到平台
			//从mongodb中查询到附近的司机（根据装货地址），距离为
			List<String> cyzIds = MongoDBUtil.findNearCyz(lon, lat, maxDistance);
			if(cyzIds == null || cyzIds.size() == 0) {
				logger.info("\n【货源管理】-货源通知-短信通知---->在mongodb中无附近司机信息   【通知结束！】");
				//if (!Kit.DEV_MODE) {
					return;
				//}
			}
			StringBuilder tempIds = new StringBuilder("");
			int len = cyzIds.size();
			for(int i=0 ;i<len;i++) {
				if(i < (len - 1)) {
					tempIds.append("'"+cyzIds.get(i)+"',");
				} else {
					tempIds.append("'"+cyzIds.get(i)+"'");
				}
			}
			logger.info("\n【货源管理】-货源通知-短信通知---->在mongodb中,匹配的到的司机有："+len+" 【通知继续！】【"+goodsName+"】");

			//从数据库中查找满足条件的司机
			Long now = System.currentTimeMillis();
			now = now - 1000 * 3600 * 48;
			Date yest = new Date(now);  // 推送时间为前两天的
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			/**
			 * 添加发送时间限制
			 */
			String sql = "select cyz.id as cyzId,cyz.`name` as cyzName, " + 
					"cyz.tel as cyzTel, car.car_type as carType " + 
					"from " + 
					"ssd_user_cyz as cyz INNER JOIN " + 
					"ssd_user_car as car ON cyz.car_id = car.id " + 
					"where cyz.id in ("+tempIds.toString()+") and cyz.state = 3 and cyz.send_time <= '" +format.format(yest)+
					"' and vlength >= "+carLenght+" and car.vlength <= "+maxCarLength;//去掉 载重的条件判断 and car.car_load >= "+weight+" 2017-11-23
			List<Record> cyzs = Db.find(sql);
			if(cyzs == null || cyzs.size() == 0) {
				logger.info("\n【货源管理】-货源通知-短信通知---->从数据库中匹配到：0条 【通知结束！】");
				return;
			}
			logger.info("\n【货源管理】-货源通知-短信通知----> 从数据库中匹配到："+cyzs.size()+"【通知继续！】");

			StringBuilder tels = new StringBuilder("");
			List<String> tels0 = new ArrayList<>();
			List<String> cyzIds0 = new ArrayList<>();

			//按车型过滤
			int telSize = 0;
			if(carType == null || carType.trim().length() == 0 || carType.equals("不限")) {//车型不限
				logger.info("\n【货源管理】-货源通知-短信通知---->车型匹配：不限【通知继续！】");
				for(int i = 0; i < cyzs.size();i++)  {
					if (telSize > MessageToal) {//短信数目的限制 2017-11-24
						logger.info("\n【货源管理】-货源通知-短信通知----> 达到最大通知数量，跳出循环【通知继续！】");
						break;
					}
					Record r = cyzs.get(i);
					if(i < (cyzs.size()-1) && (telSize < MessageToal)) {
						if(r.getStr("cyzTel") != null && r.getStr("cyzTel").trim().length() > 0) {
							tels.append(r.getStr("cyzTel") + ",");
							tels0.add(r.getStr("cyzTel"));
							cyzIds0.add(r.getStr("cyzId"));
							telSize ++;
						} 
					} else {
						if (r.getStr("cyzTel") != null && r.getStr("cyzTel").trim().length() > 0) {
							cyzIds0.add(r.getStr("cyzId"));
							tels.append(r.getStr("cyzTel"));
							tels0.add(r.getStr("cyzTel"));
							telSize ++;
						} 
					}
				}
			} else { //限制了车型
				logger.info("\n【货源管理】-货源通知-短信通知---->车型匹配："+carType+"【通知继续！】");
				List<String> telTemp = new ArrayList<>();
				for(int i = 0; i < cyzs.size();i++)  {
					Record r = cyzs.get(i);
					if(r.getStr("carType") != null && carType.indexOf(r.getStr("carType")) >=0 && r.getStr("cyzTel") != null && r.getStr("cyzTel").trim().length() > 0) {
						telTemp.add(r.getStr("cyzTel"));
						cyzIds0.add(r.getStr("cyzId"));
					}
				}
				int len1 = telTemp.size();
				logger.info("\n【货源管理】-货源通知-短信通知---->车型匹配："+len1+"【通知继续！】");
				if(len1 > 0) {
					for(int i = 0;i < len1; i++) {
						if (telSize > MessageToal) {//短信数目的限制 2017-11-24
							logger.info("\n【货源管理】-货源通知-短信通知----> 达到最大通知数量，跳出循环【通知继续！】");
							break;
						}
						if(i < (len1 -1) && (telSize < MessageToal)) {
							tels0.add(telTemp.get(i));
							tels.append(telTemp.get(i) + ",");
							
							telSize ++ ;
						}else {
							tels0.add(telTemp.get(i));
							tels.append(telTemp.get(i));
							telSize ++ ;
						}
					}
					
				}else {
					logger.info("\n【货源管理】-货源通知-短信通知---->没有对应车型 -【通知结束！】");
					return;
				}
			}
			
			//构建短信
			String money = null;;
			if(priceType == 0) {
				money = "一口价 / "+price+" 元";
			}else if(priceType == 1){
				money = "面议";
			} else {
				if(unit == 0) {
					money = price + " 元/吨 ";
				} else {
					money = price + " 元/方 ";
				}
			}
			String types = null;
			if(unit == 0) {
				types = weight + " 吨";
			} else {
				types = weight + " 方";
			}

			// 1.极光推送 2017-11-21
			if(Kit.DEV_MODE) {
				logger.info("\n【货源管理】-货源通知----->测试环境");
				tels0.clear();
				tels0.add("18580591317");
				tels0.add("18223658620");
			}
			String title = "货车专用导航";
			String type0 = "1";
			String alertContent = "附近有一单新货源，请留意！附近有一单新货源，请留意！";
			PushKit.hcdhPushMany(tels0, alertContent, alertContent, type0, reqId);
			
			// 2.发布货源给司机推送短信
			if (publish < 1) {
				return;
			}

			// 车长格式化
			String carLen = null;
			if(carLenght == null || carLenght <= 0.001) {
				carLen = "车长：不限 ";
			} else if(carLenght == maxCarLength ){
				carLen = "车长：" +carLenght+ " 米";
			} else {
				carLen = "车长：" +carLenght+ "-" +maxCarLength+ " 米";
			}

			// 车型格式化
			String car = null;
			if(carType == null || carType.length() == 0 || "不限".equals(carType) || "null".equals(carType)) {
				car = "车型不限";
			} else {
				car = carType;
			}

			// 地址格式化
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
			String deliverDate = fmt.format(deliverTime);
			String start = "";
			if (a1.getProvince().equals(a1.getCity())){
				start = a1.getCity()+a1.getDistrict();
			} else {
				start = a1.getProvince()+a1.getCity()+a1.getDistrict();
			}
			String end = "";
			if (a2.getProvince().equals(a2.getCity())){
				end = a2.getCity()+a2.getDistrict();
			} else {
				end = a2.getProvince()+a2.getCity()+a2.getDistrict();
			}

			if (Kit.DEV_MODE) {
				SMS.hyMessage("18580591317",goodsName, types, carLen, car, start, end, deliverDate, money);
				logger.info("\n【货源管理】-货源通知-短信通知---->需要发送的号码为：\n "+tels+"\n-【通知成功！】");
				return;
			} else {
				SMS.hyMessage(tels.toString(), goodsName, types, carLen, car, start, end, deliverDate, money);
				//logger.info("\n【货源管理】-货源通知-短信通知---->发出通知结束，发出的号码为：\n "+tels.toString()+"\n-【通知成功！】");
			}
			

			//**更新数据库
			int count = cyzIds0.size();
			StringBuilder sqlUpdateCzy = null;
			if (count < 1) {
				return;
			} else if (count > 1) {
				sqlUpdateCzy = new StringBuilder("update ssd_user_cyz set send_time = '" + format.format(new Date()) + "' where id in (");
				sqlUpdateCzy.append("'" + cyzIds0.get(0) + "'");
				for (int i = 1; i < count; i++) {
					sqlUpdateCzy.append(",'" + cyzIds0.get(i) + "'");
				}
				sqlUpdateCzy.append(")");
			} else {
				sqlUpdateCzy = new StringBuilder("update ssd_user_cyz set send_time = '" + format.format(new Date()) + "' where id=");
				sqlUpdateCzy.append("'" + cyzIds0.get(0) + "'");
			}
			//logger.info("\n【货源管理】-货源通知-短信通知-更新的司机:" +sqlUpdateCzy.toString()+"\n");

			int i = Db.update(sqlUpdateCzy.toString());
			if (i > 0) {
				logger.info("\n【货源管理】-货源通知-短信通知-更新司机接受短信的时间---->发送" +count+ "条【SMS_req】 " +a1.getCity());
			} else {
				logger.info("\n【货源管理】-货源通知-短信通知-更新司机接受短信的时间---->失败");
			}
			//logger.info("\n【货源管理】-货源通知-短信通知---->发出通知结束 ，发出的号码为：\n "+tels+"\n-【通知成功！】");
		}
		
	}
	
	/**
	 * 把字符串转为数据库安全字符串
	 * @param str
	 * @return
	 */
	private static String quoteString(String str) {
		if (str == null) {
			return "null";
		}
		boolean hasText = false;
		for (int i = 0; i < str.length(); i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				hasText = true;
				break;
			}
		}
		
		if (hasText) {
			return "'"+str+"'";
		}
		return "null";
	}
	private static String quoteObject(Object obj) {
		if (obj != null) {
			return quoteString(obj.toString());
		}
		return "null";
	}

	/**
	 * 验证保证金
	 * @param orderId
	 */
	public Map<String, Object> checkGuaranteeByoid(String orderId) {
		Map<String, Object> res = new HashMap<>();
		//判断订单的货源id
		String sql = "select *  from ylgj_order_bid where id='"+orderId+"'";
		List<Record> requirementsId = Db.find(sql);
		//获取订单的货源id
		String requirementId=requirementsId.get(0).getStr("requirement_id");
		//查询货源订单保证金
		String sql1 = "select *  from ylgj_order_requirement where id='"+requirementId+"'";
		List<Record> requirements = Db.find(sql1);
		if(requirements == null || requirements.size() == 0) {
			res.put("data", null);
			res.put("code", 200);
			res.put("msg", "没有该货源订单");
			return res;
		}
		//获取保证金
		BigDecimal guarantee=requirements.get(0).getBigDecimal("guarantee");
		if(guarantee != null && guarantee.compareTo(BigDecimal.ZERO)>0){
			res.put("data", guarantee);
			res.put("code", 200);
			res.put("msg", "货源有保证金不能处理");
			return res;
		}
		res.put("data", null);
		res.put("code", 200);
		res.put("msg", "货源没有保证金");
		return res;
	}

	/**
	 * 验证保证金
	 * @param requirementId
	 */
	public Map<String, Object> checkGuaranteeByqid(String requirementId) {
		Map<String, Object> res = new HashMap<>();
		//查询货源订单保证金
		String sql = "select *  from ylgj_order_requirement where id='"+requirementId+"'";
		List<Record> requirements = Db.find(sql);
		if(requirements == null || requirements.size() == 0) {
			res.put("data", null);
			res.put("code", 200);
			res.put("msg", "没有该货源订单");
			return res;
		}
		//获取保证金
		BigDecimal guarantee=requirements.get(0).getBigDecimal("guarantee");
		if(guarantee != null && guarantee.compareTo(BigDecimal.ZERO)>0){
			res.put("data", guarantee);
			res.put("code", 200);
			res.put("msg", "货源有保证金不能处理");
			return res;
		}
		res.put("data", null);
		res.put("code", 200);
		res.put("msg", "货源没有保证金");
		return res;
	}

}
