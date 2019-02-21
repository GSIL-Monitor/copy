package com.shabro.erp.vo;
import com.alibaba.fastjson.JSONObject;
import java.sql.Timestamp;
import com.shabro.erp.utils.BaseUtil;

/**
 * YlgjOrderRequirement(需求信息)的子集类。
 */
public class SubRequirementMessage {

    private String id;
    private String startAddress;        //出发市
    private String startProvince;
    private String startDistrict;
    private String startAddressDetail;
    private String startLatitude;       //(经度,纬度)

    private String arriveAddress;
    private String arriveProvince;
    private String arriveDistrict;
    private String arriveAddressDetail;
    private String arriveLatitude;      //(经度,纬度)



    private String deliverUsername;

    private String arriveUsername;

    private String goodsName;
    private String  createDate;
    private String weight;              //重货是重量，泡货时是体积，便于后面统一计算
    private String carType;

    private String  areaCode;           //发货地行政区划代码(市或区县的)
    private String  arriveCode;         //收货地行政区划代码(市或区县的)
    private String price;
    private String priceType;

    private String  dist;               //装货地到卸货地的距离(km)

    private String reqType;            //重货还是泡货 0 重货 1泡货
    private String state;//

    public SubRequirementMessage(){}

    /**
     * 根据JSONObject中相同名称的值来构造SubRequirementMessage
     * @param "JSONObject"
     */
    public SubRequirementMessage(net.sf.json.JSONObject j){
        this.id = String.valueOf(j.getString("id"));
        this.startAddress =String.valueOf(j.getString("startAddress"));
        this.startProvince = String.valueOf(j.getString("startProvince"));
        this.startDistrict = String.valueOf(j.getString("startDistrict"));
        this.startAddressDetail = String.valueOf(j.getString("startAddressDetail"));
        this.startLatitude = String.valueOf(j.getString("startLatitude"));
        this.arriveAddress = String.valueOf(j.getString("arriveAddress"));
        this.arriveProvince =String.valueOf(j.getString("arriveProvince"));
        this.arriveDistrict =String.valueOf(j.getString("arriveDistrict"));
        this.arriveAddressDetail =String.valueOf(j.getString("arriveAddressDetail"));
        this.arriveLatitude =String.valueOf(j.getString("arriveLatitude"));
        this.deliverUsername =String.valueOf(j.getString("deliverUsername"));
        this.arriveUsername = String.valueOf(j.getString("arriveUsername"));
        this.goodsName = String.valueOf(j.getString("goodsName"));
        this.createDate = String.valueOf(j.getString("createDate"));
        this.weight =String.valueOf(j.getString("weight"));
        this.carType =String.valueOf(j.getString("carType"));
        this.areaCode = String.valueOf(j.getString("areaCode"));
        this.arriveCode =String.valueOf(j.getString("arriveCode"));
        this.price =String.valueOf(j.getString("price"));
        this.priceType =String.valueOf(j.getString("priceType"));
        this.dist = String.valueOf(j.getString("dist"));
        this.reqType =String.valueOf(j.getString("reqType"));
        this.state =String.valueOf(j.getString("state"));

    }
    /**
     * 根据YlgjOrderRequirement对象中的属性值来构造SubRequirementMessage（将YlgjOrderRequirement转化为SubRequirementMessage）
     * @param "YlgjOrderRequirement"
     */
  /*  public SubRequirementMessage(YlgjOrderRequirement y){
        this.id = y.getId();
        this.startAddress = y.getStartAddress();
        this.startProvince = y.getStartProvince();
        this.startDistrict = y.getStartDistrict();
        this.startAddressDetail = y.getStartAddressDetail();
        this.startLatitude = y.getStartLatitude();
        this.arriveAddress = y.getArriveAddress();
        this.arriveProvince = y.getArriveProvince();
        this.arriveDistrict =y.getArriveDistrict();
        this.arriveAddressDetail = y.getArriveAddressDetail();
        this.arriveLatitude = y.getArriveLatitude();
        this.deliverUsername = y.getDeliverUsername();
        this.arriveUsername = y.getArriveUsername();
        this.goodsName = y.getGoodsName();
        this.createDate = BaseUtil.timeToString(y.getCreateDate());
        this.weight =String.valueOf(y.getWeight());
        this.carType =y.getCarType();
        this.areaCode = y.getAreaCode();
        this.arriveCode = y.getArriveCode();
        this.price =String.valueOf( y.getPrice());
        this.priceType =String.valueOf( y.getPriceType());
        this.dist = String.valueOf( y.getDist() );
        this.reqType = String.valueOf(y.getReqType());
        this.state = String.valueOf(y.getState());
    }*/
    /**
     * 根据提供的值来构建SubRequirementMessage
     * @param(String id, String startAddress, String startProvince,
    String startDistrict, String startAddressDetail,
    String startLatitude, String arriveAddress,
    String arriveProvince, String arriveDistrict,
    String arriveAddressDetail, String arriveLatitude,
    String deliverUsername, String arriveUsername,
    String goodsName, Timestamp createDate, Double weight,
    String carType, String areaCode, String arriveCode,
    Double price, Integer priceType, Double dist, Integer reqType)
     */
    public SubRequirementMessage(String id, String startAddress, String startProvince,
                                 String startDistrict, String startAddressDetail,
                                 String startLatitude, String arriveAddress,
                                 String arriveProvince, String arriveDistrict,
                                 String arriveAddressDetail, String arriveLatitude,
                                 String deliverUsername, String arriveUsername,
                                 String goodsName, Timestamp createDate, Double weight,
                                 String carType, String areaCode, String arriveCode,
                                 Double price, Integer priceType, Double dist, Integer reqType,Integer state) {

        this.id = id;
        this.startAddress = startAddress;
        this.startProvince = startProvince;
        this.startDistrict = startDistrict;
        this.startAddressDetail = startAddressDetail;
        this.startLatitude = startLatitude;
        this.arriveAddress = arriveAddress;
        this.arriveProvince = arriveProvince;
        this.arriveDistrict = arriveDistrict;
        this.arriveAddressDetail = arriveAddressDetail;
        this.arriveLatitude = arriveLatitude;
        this.deliverUsername = deliverUsername;
        this.arriveUsername = arriveUsername;
        this.goodsName = goodsName;
        this.createDate = BaseUtil.timeToString(createDate);
        this.weight = String.valueOf(weight);
        this.carType = carType;
        this.areaCode = areaCode;
        this.arriveCode = arriveCode;
        this.price = String.valueOf(price);
        this.priceType = String.valueOf(priceType);
        this.dist = String.valueOf(dist);
        this.reqType = String.valueOf(reqType);
        this.state=String.valueOf(state);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getStartProvince() {
        return startProvince;
    }

    public void setStartProvince(String startProvince) {
        this.startProvince = startProvince;
    }

    public String getStartDistrict() {
        return startDistrict;
    }

    public void setStartDistrict(String startDistrict) {
        this.startDistrict = startDistrict;
    }

    public String getStartAddressDetail() {
        return startAddressDetail;
    }

    public void setStartAddressDetail(String startAddressDetail) {
        this.startAddressDetail = startAddressDetail;
    }

    public String getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(String startLatitude) {
        this.startLatitude = startLatitude;
    }

    public String getArriveAddress() {
        return arriveAddress;
    }

    public void setArriveAddress(String arriveAddress) {
        this.arriveAddress = arriveAddress;
    }

    public String getArriveProvince() {
        return arriveProvince;
    }

    public void setArriveProvince(String arriveProvince) {
        this.arriveProvince = arriveProvince;
    }

    public String getArriveDistrict() {
        return arriveDistrict;
    }

    public void setArriveDistrict(String arriveDistrict) {
        this.arriveDistrict = arriveDistrict;
    }

    public String getArriveAddressDetail() {
        return arriveAddressDetail;
    }

    public void setArriveAddressDetail(String arriveAddressDetail) {
        this.arriveAddressDetail = arriveAddressDetail;
    }

    public String getArriveLatitude() {
        return arriveLatitude;
    }

    public void setArriveLatitude(String arriveLatitude) {
        this.arriveLatitude = arriveLatitude;
    }

    public String getDeliverUsername() {
        return deliverUsername;
    }

    public void setDeliverUsername(String deliverUsername) {
        this.deliverUsername = deliverUsername;
    }

    public String getArriveUsername() {
        return arriveUsername;
    }

    public void setArriveUsername(String arriveUsername) {
        this.arriveUsername = arriveUsername;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }


    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getCarType() {
        return carType;
    }

    public void setCarType(String carType) {
        this.carType = carType;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getArriveCode() {
        return arriveCode;
    }

    public void setArriveCode(String arriveCode) {
        this.arriveCode = arriveCode;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    public String getDist() {
        return dist;
    }

    public void setDist(String dist) {
        this.dist = dist;
    }

    public String getReqType() {
        return reqType;
    }

    public void setReqType(String reqType) {
        this.reqType = reqType;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}

