package com.shabro.erp.consignor.resource;

import com.jfinal.aop.Duang;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.lastb7.dubbo.plugin.spring.Inject;
import com.shabro.erp.consignor.common.kit.Kit;
import com.shabro.erp.consignor.resource.RequirementService.Address;
import com.shabro.erp.vo.InvoiceVo;
import com.shabro.rpc.service.blockchain.BlockChainService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 货源管理
 *
 * @author kui
 * @version 1.1 on 2017-9-14
 */
public class RequirementController extends Controller {


    /**
     * 查询后台是否支持开票
     */
    public void getFunctionVisible() {
        RequirementService service = Duang.duang(RequirementService.class, Tx.class);
        setAttrs(Kit.toMap(service.getFunctionVisible(), "sucess", Kit.CODE_LOGIN_TRUE));
        renderJson();
    }

    /**
     * 获取邮费
     */
    public void getPostage() {
        RequirementService service = Duang.duang(RequirementService.class, Tx.class);
        setAttrs(Kit.toMap(service.getPostage(), "sucess", Kit.CODE_LOGIN_TRUE));
        renderJson();
    }


    /**
     * 保存发票信息模板
     */
    public void submitInvoice() {
        String fbzId = getPara("fbzId"); // 发布者id
        String invoiceTitle = getPara("invoiceTitle"); // 发票抬头
        String invoiceContent = getPara("invoiceContent"); // 发票内容
        String accountNum = getPara("accountNum"); // 开票的账号
        String dutyNum = getPara("dutyNum"); // 开票的税号
        String accountBank = getPara("accountBank"); // 开户行
        String consignee = getPara("consignee"); // 收件人
        String consigneePhone = getPara("consigneePhone"); // 收件人电话
        String consigneeAddress = getPara("consigneeAddress");// 收件详细地址
        String companyAddress = getPara("companyAddress");//公司地址 2018-01-18
        String companyTel = getPara("companyTel");//公司联系电话 2018-01-18
        String postage = getPara("postage", "0.0");//邮费


        InvoiceVo invoiceVo = new InvoiceVo();
        invoiceVo.setFbzId(fbzId);
        invoiceVo.setInvoiceTitle(invoiceTitle);
        invoiceVo.setInvoiceContent(invoiceContent);
        invoiceVo.setInvoiceContent("物流服务费");
        invoiceVo.setAccountNum(accountNum);
        invoiceVo.setDutyNum(dutyNum);
        invoiceVo.setAccountBank(accountBank);
        invoiceVo.setConsignee(consignee);
        invoiceVo.setConsigneePhone(consigneePhone);
        invoiceVo.setConsigneeAddress(consigneeAddress);
        invoiceVo.setCompanyAddress(companyAddress);
        invoiceVo.setCompanyTel(companyTel);
        //邮费
        invoiceVo.setPostage(Double.valueOf(postage));
        RequirementService service = Duang.duang(RequirementService.class, Tx.class);
        setAttrs(Kit.toMap(service.saveOrUpdateInvoice(invoiceVo)
                , "sucess", Kit.CODE_LOGIN_TRUE));
        renderJson();

    }


    /**
     * 获取发票模板数据
     */
    public void getInvoice() {
        String fbzId = getPara("fbzId", null);
        RequirementService service = Duang.duang(RequirementService.class, Tx.class);
        setAttrs(Kit.toMap(service.getInvoice(fbzId), "sucess", Kit.CODE_LOGIN_TRUE));
        renderJson();
    }


    /**
     * 获取开票税率列表
     */
    public void getTaxRateList() {
        RequirementService service = Duang.duang(RequirementService.class, Tx.class);
        setAttrs(Kit.toMap(service.getTaxRateList(), "sucess", Kit.CODE_LOGIN_TRUE));
        renderJson();
    }


    /**
     * @param token|验证秘钥|String|必选
     * @param pageNo|页码——》=1|Integer|是
     * @param pageSize|每页的大小——默认为10|Integer|否
     * @param fbzId|货主id|String|是
     * @param id|流水号|String|否
     * @param isInsurance|是否有保险——0-没有，1-有|String|否
     * @param state|状态——0：接单中；1：确定中标人；2：已完成；3：取消；4：已停止发布|Integer|否
     * @param dateStart|开始时间条件——格式：2017-08-07                      15:33:33|String|否
     * @param dateEnd|结束时间条件|String|否
     * @title 获取货主的货源列表
     */
    @SuppressWarnings("unchecked")
    public void listByPage() {
        Integer pageNo = getParaToInt("pageNo", 1);
        Integer pageSize = getParaToInt("pageSize", 10);

        String fbzId = getPara("fbzId", null);
        String id = getPara("id", null);
        Integer isInsurance = getParaToInt("isInsurance", null);
        Integer state = getParaToInt("state", null);
        String dateStart1 = getPara("dateStart", null);
        String dateEnd1 = getPara("dateEnd", null);
        Date dateStart = null;
        Date dateEnd = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (dateStart1 != null) {
            try {
                dateStart = format.parse(dateStart1);
            } catch (ParseException e) {
                e.printStackTrace();
                dateStart = null;
            }
        }
        if (dateEnd1 != null) {
            try {
                dateEnd = format.parse(dateEnd1);
            } catch (ParseException e) {
                e.printStackTrace();
                dateEnd = null;
            }
        }
        //dateStart = getParaToDate("dateStart",null);
        //dateEnd = getParaToDate("dateEnd",null);
        if (fbzId == null || fbzId.trim().length() == 0) {
            setAttrs(Kit.toMap(null, "参数错误", Kit.ERROR_CODE_PARA));
        } else {
            RequirementService service = Duang.duang(RequirementService.class, Tx.class);
            setAttrs(service.list(fbzId, pageNo, pageSize, id, isInsurance, state, dateStart, dateEnd));
        }

        renderJson();
    }

    /**
     * @param token|验证秘钥|String|必选 获取货源详细 和与货源对应的订单列表
     * @param id|货源流水号|String|是
     * @title 获取详细
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public void getDetail() {
        String id = getPara("id");

        if (id == null || id.trim().length() == 0) {
            setAttrs(Kit.toMap(null, "参数错误", Kit.ERROR_CODE_PARA));
        } else {
            RequirementService service = Duang.duang(RequirementService.class, Tx.class);
            setAttrs(Kit.toMap(service.details(id), "sucess", Kit.CODE_LOGIN_TRUE));
        }
        renderJson();
    }

    /**
     * @param token|验证秘钥|String|必选             获取地址：发货地址+收货地址
     * @param tel|电话号码|String|是
     * @param type|0-获取发货地址，1-获取收获地址|Integer|是
     * @title 获取发货地址+收货地址
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public void getAddress() {
        String id = getPara("fbzId");
        String tel = getPara("tel");
        Integer type = getParaToInt("type");
        if (tel == null || tel.trim().length() == 0) {
            setAttrs(Kit.toMap(null, "参数错误", Kit.ERROR_CODE_PARA));
        } else {
            RequirementService service = Duang.duang(RequirementService.class, Tx.class);
            setAttrs(Kit.toMap(service.getAddress(id, type, tel), "success", Kit.CODE_LOGIN_TRUE));
        }
        renderJson();
    }

    /**
     * @param token|验证秘钥|String|必选 获取后去类型名称
     * @param fbzId|货主的id|String|是
     * @title 查询货物类型
     */
    public void getGoodsName() {
        String id = getPara("fbzId");
        if (id == null || id.trim().length() == 0) {
            setAttrs(Kit.toMap(null, "参数错误", Kit.ERROR_CODE_PARA));
        } else {
            RequirementService service = Duang.duang(RequirementService.class, Tx.class);
            setAttrs(Kit.toMap(service.getGoodsName(id), "success", Kit.CODE_LOGIN_TRUE));
        }
        renderJson();
    }

    /**
     * 删除货主自定义的货物类型
     *
     * @since 1.1
     */
    public void delGoodsType() {
        String fbzId = getPara("fbzId");
        Integer id = getParaToInt("id");
        Map<String, Object> res = new HashMap<>();
        if (fbzId == null || fbzId.trim().length() == 0 || id == null) {
            res.put("code", 1);
            res.put("msg", "参数错误");
        } else {
            RequirementService service = Duang.duang(RequirementService.class, Tx.class);
            try {
                service.delGoodsType(fbzId, id, res);
            } catch (Exception e) {
                e.printStackTrace();
                res.put("code", 1);
                res.put("msg", "操作失败");
            }
        }
        setAttrs(res);
        renderJson();
    }

    /**
     * 添加货物类型
     * 注：货主最多可有四个自定义的，当添加超过四个，就会覆盖前面添加的
     *
     * @since 1.1
     */
    public void addGoodsType() {
        String fbzId = getPara("fbzId");
        String goodsnName = getPara("goodsName");
        Map<String, Object> res = new HashMap<>();
        if (fbzId == null || fbzId.trim().length() == 0) {
            res.put("code", 1);
            res.put("msg", "参数错误");
        } else {
            RequirementService service = Duang.duang(RequirementService.class, Tx.class);
            try {
                service.addGoodsType(fbzId, goodsnName, res);
            } catch (Exception e) {
                e.printStackTrace();
                res.put("code", 1);
                res.put("msg", "操作失败");
            }
        }
        setAttrs(res);
        renderJson();
    }

    /**
     * @param token|验证秘钥|String|必选 获取车辆类型和长度
     * @title 获取车辆类型和长度
     */
    public void getCarInfo() {
        RequirementService service = Duang.duang(RequirementService.class, Tx.class);
        setAttrs(Kit.toMap(service.getCarInfo(), "success", Kit.CODE_LOGIN_TRUE));

        renderJson();

    }

    /**
     * 发布货源
     *
     * @param token|验证秘钥|String|必选
     * @param fbzId|货主的id|String|是
     * @param startAddressId|发货地址的id——假如没有选择，填-1|Integer|是
     * @param arriveAddressId|收货地址的id——假如没有选择，填-1|Integer|是
     * @param goodsName|货物名称|String|是
     * @param weight|货物的重量或体积|double|是
     * @param unit|单位——0：吨，1：立方|String|是
     * @param deliverTime|装货时间|String|是
     * @param arriveLimit|到达期限——单位：天|Integer|是
     * @param carTypeName|车辆类型名|String|是
     * @param carLenght|车辆长度|String|是
     * @param maxCarLength|车辆最大长度|String|是
     * @param priceType|价格类型——0                             口价 1 价格面议 2 单价|integer|是
     * @param price|价签|Double|否
     * @param needInvoice|发票  1—3.36   2—10.   0 其他（不需要）
     * @param needInsurance|是否需要保险——0：不需要，1：需要|integer|是
     * @param scope|发送给——0                                  所有承运人；1我的车队|integer|是
     * @param price|价签|Double|否
     * @param startPhone|发货人电话|String|是
     * @param startUser|发货联系人|String|是
     * @param startProvince|发货省份|String|是
     * @param startCity|发货城市|String|是
     * @param startDistrict|发货区县|String|是
     * @param startAddressDetail|发货详细地址|String|是
     * @param startLon|发货经度|double|是
     * @param startLat|发货纬度|double|是
     * @param arrivePhone|——|String|是
     * @param arriveUser|——|String|是
     * @param arriveProvince|——|String|是
     * @param arriveCity|——|String|是
     * @param arriveDistrict|——|String|是
     * @param arriveAddressDetail|——|String|是
     * @param arriveLon|——|String|是
     * @param arriveLat|——|String|是
     * @param dist|运输距离，单位km|double|否
     * @title 发布货源
     */

    public void publish() {
        String requirementId = getPara("requirementId", null);
        String fbzId = getPara("fbzId", null);
        Integer startAddressId = getParaToInt("startAddressId", null);
        Integer arriveAddressId = getParaToInt("arriveAddressId", null);
        String goodsName = getPara("goodsName", null);
        Double weight = new Double(getPara("weight", "0.0"));
        Integer unit = getParaToInt("unit", 0);
        Date deliverTime = getParaToDate("deliverTime", null);//装货日期
        Integer arriveLimit = getParaToInt("arriveLimit", null);//到达时限（天）
        String carTypeName = getPara("carTypeName", null);
        Double carLenght = new Double(getPara("carLenght", "0.0"));
        Double maxCarLength = new Double(getPara("maxCarLength", "100.0"));
        Integer priceType = getParaToInt("priceType", null);
        Double price = new Double(getPara("price", "0.00"));
        Integer needInvoice = getParaToInt("needInvoice", 0);
        Integer needInsurance = getParaToInt("needInsurance", 0);
        //scope
        Integer scope = getParaToInt("scope");


        String startPhone = getPara("startPhone");
        String startUser = getPara("startUser");
        String startProvince = getPara("startProvince");
        String startCity = getPara("startCity");
        String startDistrict = getPara("startDistrict");
        String startAddressDetail = getPara("startAddressDetail");
        String areaCode = getPara("areaCode");
        Double startLon = new Double(getPara("startLon", "0.0"));
        Double startLat = new Double(getPara("startLat", "0.0"));
        Address startAddress = new Address(startPhone, startUser, startLon, startLat, startCity + startDistrict, startCity, startDistrict, startProvince, startAddressDetail);

        String arrivePhone = getPara("arrivePhone");
        String arriveUser = getPara("arriveUser");
        String arriveProvince = getPara("arriveProvince");
        String arriveCity = getPara("arriveCity");
        String arriveDistrict = getPara("arriveDistrict");
        String arriveAddressDetail = getPara("arriveAddressDetail");
        String arriveCode = getPara("arriveCode");
        Double arriveLon = new Double(getPara("arriveLon", "0.0"));
        Double arriveLat = new Double(getPara("arriveLat", "0.0"));
        Address arriveAddress = new Address(arrivePhone, arriveUser, arriveLon, arriveLat, arriveCity + arriveDistrict, arriveCity, arriveDistrict, arriveProvince, arriveAddressDetail);

        Double dist = new Double(getPara("dist", null));


        //对于不限的处理
        if (carTypeName == null || carTypeName.trim().length() == 0 || carTypeName.indexOf("不限") != -1) {
            carTypeName = null;
        }
        if (scope != 0) {
            scope = 1;
        }
        //放开 || startAddressId == null || arriveAddressId == null
        boolean codeIsNull = Kit.stringsIsBlank(areaCode, arriveCode);
        if (fbzId == null || goodsName == null || codeIsNull) {
            Map<String, Object> res = new HashMap<>();
            res.put("data", null);
            res.put("code", 1);
            res.put("msg", "参数错误");
            setAttrs(res);
        } else {


            RequirementService service = Duang.duang(RequirementService.class, Tx.class);


            if (requirementId == null || requirementId.trim().length() == 0) {
                try {
                    Map<String, Object> res = new HashMap<>();
                    int i = service.publish(fbzId, startAddressId, arriveAddressId, goodsName, weight, unit, deliverTime, arriveLimit, carTypeName, carLenght, maxCarLength, priceType, needInvoice, needInsurance, scope,
                            startAddress, arriveAddress, price, dist, areaCode, arriveCode);
                    if (i == 2) {
                        res.put("data", null);
                        res.put("code", 2);
                        res.put("msg", "该货源存在保证金，暂时无法修改货源，请在运力管家APP进行操作!");
                    } else {
                        setAttrs(Kit.toMap(null, "success", Kit.CODE_LOGIN_TRUE));

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    setAttrs(Kit.toMap(null, "error", Kit.ERROR_CODE_PROCESS));
                }
            } else {
                Map<String, Object> res = new HashMap<>();
                try {
                    int i = service.update(requirementId, fbzId, startAddressId, arriveAddressId, goodsName, weight, unit, deliverTime, arriveLimit, carTypeName, carLenght, maxCarLength, priceType, needInvoice, needInsurance, scope,
                            startAddress, arriveAddress, price, dist, areaCode, arriveCode);
                    if (i == 1) {
                        res.put("data", null);
                        res.put("code", 200);
                        res.put("msg", "success");


                    } else if (i == 2) {
                        res.put("data", null);
                        res.put("code", 2);
                        res.put("msg", "该货源存在保证金，暂时无法修改货源，请在运力管家APP进行操作!");
                    } else {
                        res.put("data", null);
                        res.put("code", 1);
                        res.put("msg", "该货源不支持修改");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    res.put("data", null);
                    res.put("code", 1);
                    res.put("msg", "操作失败");
                }
                setAttrs(res);
            }
        }
        renderJson();
    }


    /**
     * 更新
     *
     * @param token|验证秘钥|String|必选
     * @param fbzId|货主的id|String|是
     * @param requirementId|货物流水号|String|是
     * @param startAddressId|发货地址的id——假如没有选择，填-1|Integer|是
     * @param arriveAddressId|收货地址的id——假如没有选择，填-1|Integer|是
     * @param goodsName|货物名称|String|是
     * @param weight|货物的重量或体积|double|是
     * @param unit|单位——0：吨，1：立方|String|是
     * @param deliverTime|装货时间|String|是
     * @param arriveLimit|到达期限——单位：天|Integer|是
     * @param carTypeName|车辆类型名|String|是
     * @param carLenght|车辆长度|String|是
     * @param priceType|价格类型——0                             口价 1 价格面议 2 单价|integer|是
     * @param price|价签|Double|否
     * @param needInvoice|是否需要发票——0：不需要，1：需要|integer|是
     * @param needInsurance|是否需要保险——0：不需要，1：需要|integer|是
     * @param scope|发送给——0                                  所有承运人；1我的车队|integer|是
     * @param price|价签|Double|否
     * @param startPhone|发货人电话|String|是
     * @param startUser|发货联系人|String|是
     * @param startProvince|发货省份|String|是
     * @param startCity|发货城市|String|是
     * @param startDistrict|发货区县|String|是
     * @param startAddressDetail|发货详细地址|String|是
     * @param startLon|发货经度|double|是
     * @param startLat|发货纬度|double|是
     * @param arrivePhone|——|String|是
     * @param arriveUser|——|String|是
     * @param arriveProvince|——|String|是
     * @param arriveCity|——|String|是
     * @param arriveDistrict|——|String|是
     * @param arriveAddressDetail|——|String|是
     * @param arriveLon|——|String|是
     * @param arriveLat|——|String|是
     * @title 更新货源信息
     */
    public void update() {
        String requirementId = getPara("requirementId", null);
        String fbzId = getPara("fbzId", null);
        Integer startAddressId = getParaToInt("startAddressId", null);
        Integer arriveAddressId = getParaToInt("arriveAddressId", null);
        String goodsName = getPara("goodsName", null);
        Double weight = new Double(getPara("weight", "0.0"));
        Integer unit = getParaToInt("unit", 0);
        Date deliverTime = getParaToDate("deliverTime", null);//装货日期
        Integer arriveLimit = getParaToInt("arriveLimit", null);//到达时限（天）
        String carTypeName = getPara("carTypeName", null);
        //对于不限的处理
        if (carTypeName.indexOf("不限") != -1) {
            carTypeName = null;
        }
        Double carLenght = new Double(getPara("carLenght", "0.0"));
        Double maxCarLength = new Double(getPara("maxCarLength", "100.0"));
        Integer priceType = getParaToInt("priceType", null);
        Double price = new Double(getPara("price", "0.00"));
        Integer needInvoice = getParaToInt("needInvoice", 0);
        Integer needInsurance = getParaToInt("needInsurance", 0);
        //scope
        Integer scope = getParaToInt("scope");

        String startPhone = getPara("startPhone");
        String startUser = getPara("startUser");
        String startProvince = getPara("startProvince");
        String startCity = getPara("startCity");
        String startDistrict = getPara("startDistrict");
        String startAddressDetail = getPara("startAddressDetail");
        String areaCode = getPara("areaCode");
        Double startLon = new Double(getPara("startLon", "0.0"));
        Double startLat = new Double(getPara("startLat", "0.0"));
        Address startAddress = new Address(startPhone, startUser, startLon, startLat, startCity + startDistrict, startCity, startDistrict, startProvince, startAddressDetail);

        String arrivePhone = getPara("arrivePhone");
        String arriveUser = getPara("arriveUser");
        String arriveProvince = getPara("arriveProvince");
        String arriveCity = getPara("arriveCity");
        String arriveDistrict = getPara("arriveDistrict");
        String arriveAddressDetail = getPara("arriveAddressDetail");
        String arriveCode = getPara("arriveCode");
        Double arriveLon = new Double(getPara("arriveLon", "0.0"));
        Double arriveLat = new Double(getPara("arriveLat", "0.0"));
        Address arriveAddress = new Address(arrivePhone, arriveUser, arriveLon, arriveLat, arriveCity + arriveDistrict, arriveCity, arriveDistrict, arriveProvince, arriveAddressDetail);

        Double dist = new Double(getPara("dist", null));
        if (scope != 0) {
            scope = 1;
        }
        Map<String, Object> res = new HashMap<>();
        //放开这个条件  || startAddressId == null || arriveAddressId == null
        if (fbzId == null || goodsName == null
                || deliverTime == null || arriveLimit == null || carTypeName == null ||
                priceType == null || requirementId == null) {

            res.put("data", null);
            res.put("code", 1);
            res.put("msg", "参数错误");

        } else {
            RequirementService service = Duang.duang(RequirementService.class, Tx.class);
            try {
                int i = service.update(requirementId, fbzId, startAddressId, arriveAddressId, goodsName, weight, unit, deliverTime, arriveLimit, carTypeName, carLenght, maxCarLength, priceType, needInvoice, needInsurance, scope,
                        startAddress, arriveAddress, price, dist, areaCode, arriveCode);
                if (i == 1) {
                    res.put("data", null);
                    res.put("code", 200);
                    res.put("msg", "success");

                } else {
                    res.put("data", null);
                    res.put("code", 1);
                    res.put("msg", "该货源不支持修改");
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.put("data", null);
                res.put("code", 1);
                res.put("msg", "操作失败");
            }
        }
        setAttrs(res);
        renderJson();
    }

    /**
     * 获取竞标列表
     *
     * @param token|验证秘钥|String|必选
     * @param fbzId|货主的id|String|是
     * @param requirementId|货物流水号|String|是
     * @param pageNo|页码|Integer|是
     * @param pageSize|每页数量-默认为0|Integer|否
     * @title 竞标列表
     */
    public void listCompeteCyz() {
        String fbzId = getPara("fbzId", null);
        String requirementId = getPara("requirementId", null);
        Integer pageNo = getParaToInt("pageNo");
        Integer pageSize = getParaToInt("pageSize");
        pageNo = pageNo != null && pageNo > 0 ? pageNo : 1;
        pageSize = pageSize != null && pageSize > 0 ? pageNo : 10;
        Map<String, Object> res = new HashMap<>();
        if (fbzId == null || requirementId == null || requirementId.trim().length() == 0 || fbzId.trim().length() == 0) {
            res.put("data", null);
            res.put("code", 1);
            res.put("msg", "参数错误");
        } else {
            RequirementService service = Duang.duang(RequirementService.class, Tx.class);
            res.put("data", service.listCompeteCyz(fbzId, requirementId, pageNo, pageSize));
            res.put("code", 200);
            res.put("msg", "success");
        }
        setAttrs(res);
        renderJson();
    }

    /**
     * 选择司机
     *
     * @param token|验证秘钥|String|必选
     * @param orderId|订单号|String|是
     * @param requirementId|货物流水号|String|是
     * @title 选择司机承运
     */
    public void chooseCyz() {
        String orderId = getPara("orderId");
        String requirementId = getPara("requirementId");
        Map<String, Object> res = new HashMap<>();
        if (orderId == null || orderId.trim().length() == 0) {
            res.put("data", null);
            res.put("code", 1);
            res.put("msg", "参数错误");
            setAttrs(res);
        } else {
            try {
                RequirementService service = Duang.duang(RequirementService.class, Tx.class);
                setAttrs(service.choose(orderId, requirementId));


            } catch (Exception e) {
                e.printStackTrace();
                res.put("data", null);
                res.put("code", 1);
                res.put("msg", "操作错误");
                setAttrs(res);
            }

        }
        renderJson();
    }

    /**
     * 拒绝
     *
     * @param token|验证秘钥|String|必选
     * @param orderId|订单号|String|是
     * @title 拒绝司机承运
     */
    public void refuse() {
        String orderId = getPara("orderId");
        Map<String, Object> res = new HashMap<>();
        if (orderId == null || orderId.trim().length() == 0) {
            res.put("data", null);
            res.put("code", 1);
            res.put("msg", "参数错误");
            setAttrs(res);
        } else {
            try {
                RequirementService service = Duang.duang(RequirementService.class, Tx.class);
                setAttrs(service.refuse(orderId));
            } catch (Exception e) {
                e.printStackTrace();
                res.put("data", null);
                res.put("code", 1);
                res.put("msg", "操作失败");
                setAttrs(res);
            }
        }
        renderJson();
    }

    /**
     * 停止发布
     *
     * @param token|验证秘钥|String|必选
     * @param requirementId|货源流水号|String|是
     * @title 停止发布
     */
    public void stop() {
        String requirementId = getPara("requirementId", null);
        Map<String, Object> res = new HashMap<>();
        if (requirementId == null || requirementId.trim().length() == 0) {
            res.put("data", null);
            res.put("code", 1);
            res.put("msg", "参数错误");
            setAttrs(res);
        } else {
            try {
                RequirementService service = Duang.duang(RequirementService.class, Tx.class);
                setAttrs(service.stop(requirementId));

            } catch (Exception e) {
                e.printStackTrace();
                res.put("data", null);
                res.put("code", 1);
                res.put("msg", "操作失败");
                setAttrs(res);
            }
        }

        renderJson();

    }

    /**
     * 是否能进行操作，货源信息修改
     */
    public void canEditRequirement() {
        String requirementId = getPara("requirementId");
        String fbzId = getPara("fbzId");
        Map<String, Object> res = new HashMap<>();
        if (requirementId == null || fbzId == null) {
            res.put("data", null);
            res.put("code", 1);
            res.put("msg", "参数错误");
        } else {
            RequirementService service = Duang.duang(RequirementService.class, Tx.class);
            service.canEditRequirement(fbzId, requirementId, res);
        }
        setAttrs(res);
        renderJson();
		
	}

    @Inject.BY_NAME
    private BlockChainService blockChainService;
	//debug
	public void find() {
		RequirementService service = Duang.duang(RequirementService.class,Tx.class);
		service.findById();
	}

    /**
     * 验证保证金
     * @param orderId|订单号|String|是
     * @title 验证保证金
     *
     */
//	public void checkGuarantee(String orderId) {
//		//String orderId ="B201804021618437862";
//		Map<String, Object> res = new HashMap<>();
//		if(orderId  == null || orderId.trim().length() == 0) {
//			res.put("data", null);
//			res.put("code", 1);
//			res.put("msg", "参数错误");
//			setAttrs(res);
//		}else {
//			try {
//				RequirementService service = Duang.duang(RequirementService.class,Tx.class);
//				setAttrs(service.checkGuaranteeByoid(orderId));
//			} catch (Exception e) {
//				e.printStackTrace();
//				res.put("data", null);
//				res.put("code", 1);
//				res.put("msg", "操作错误");
//				setAttrs(res);
//			}
//		}
//		renderJson();
//	}

}
