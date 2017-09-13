# anima

anmia 是基于java游戏服务器框架，它是高性能、高可伸缩、分布式多进程的游戏服务器框架，包括基础开发框和库，可以帮助游戏开发人员省去枯燥的重复劳动和底层逻辑工作，让开发人员只关心具体的游戏逻辑，从而提供开发效率。

# anima-sever 包结构说明
 	backend		提供后端Server服务
 	blacklist 	网络黑名单处理模块
	channel	    后端channel服务，方便推送、广播消息给客户端
	client	    异步client实现，用于后端服务器之间网络通讯
	common     公共库：包括编解码库、线程池、公共模块库、工具类等等
	fronend    提供前端Server服务
	handler	   消息处理器模块
	protocol   包含消息协议定义
	remoting	网络通讯底层模块
	route	前端server 路由模块
	session	Client session 和 Server sesisin
	surrogate	前端代理模块，前端服务器与后端服务器通讯的桥梁
# 代码commit flag说明
	feature -  新功能.
	imp     -  improvment简写，改进现有功能.
	ref     -  regactor简写，代码重构.
	fix     -  修复bug.
	test    -  测试相关.
	review  -  code review后添加的TODO 标记，说明或改动.
	res     -  引入资源.
#　anima 近期开发计划

1. 完善前端服务器（连接服务器）功能，前端服可以为后端提供服务，并且前端提供服务与为后端提供服务之间隔离，客服端不能使用后端的服务。
2.服务器提供接口给应用层，如连接事件，重连事件
3. 改进客户端请求服务器支持无请求体也就是请求的体可以为空；服务器响应客户端可以没有响应体。
4. 性能测试。

public class PreArriveServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(CarPreArriveServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("#### Do post");
        try {
            doHandler(req, resp);
        } catch (Exception e) {
            PrintWriter out = null;
            try {
                out = resp.getWriter();
                CarPreArriveResponse newSuccessInstane = CarPreArriveResponse.newSuccessInstane();
                String response = JSON.toJSONString(newSuccessInstane);
                out.write(response);
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("#### Do get");
        try {
            doHandler(req, resp);
        } catch (Exception e) {
            PrintWriter out = null;
            try {
                out = resp.getWriter();
                CarPreArriveResponse newSuccessInstane = CarPreArriveResponse.newSuccessInstane();
                String response = JSON.toJSONString(newSuccessInstane);
                out.write(response);
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }
    }

    private void doHandler(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String userAgent = getUserAgent(req);
        logger.info("UserAgent : {}", userAgent);
        if (validateUserAgentFormat(userAgent)) {
            UserAgentInfo userAgentInfo = UserAgentInfo.of(userAgent);
            BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            String md5 = userAgentInfo.getMd5();
            String content = sb.toString();
            logger.info("Content : {} ,md5{}", content, md5);
            String result = Decrypt(content, md5);
            logger.info("Decode result : {}" + result);

            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json; charset=utf-8");
            PrintWriter out = null;
            try {
                out = resp.getWriter();
                CarPreArriveResponse newSuccessInstane = CarPreArriveResponse.newSuccessInstane();
                String response = JSON.toJSONString(newSuccessInstane);
                out.write(response);
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        } else {
            PrintWriter out = null;
            try {
                out = resp.getWriter();
                CarPreArriveResponse newSuccessInstane = CarPreArriveResponse.newSuccessInstane();
                String response = JSON.toJSONString(newSuccessInstane);
                out.write(response);
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }
    }

    private String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    public boolean validateUserAgentFormat(String userAgent) {
        boolean result = false;
        if (userAgent != null && !"".equals(userAgent)) {
            //从请求header中取出User-Agent，取出后格式如下{0}_{1}_{2}_{3}
            int indexOf = userAgent.indexOf('_');
            if (indexOf != -1) {
                String[] split = userAgent.split("_");
                if (split.length == 4) {
                    String md5 = split[4];
                    char index = md5.charAt(md5.length() - 1);
                    if (!Character.isDigit(index)) {
                        logger.error("Request error,md5 index error {}, {}", md5, index);
                        return false;
                    }
                    return true;
                } else {
                    logger.error("Request error,user agent format error {}", userAgent);
                }
            } else {
                logger.error("Request error,can not found underline in user agent {}", userAgent);
            }
        }

        return result;
    }

    // 解密
    public static String Decrypt(String sSrc, String sKey) throws Exception {
        try {
            // 判断Key是否正确
            if (sKey == null) {
                System.out.print("Key为空null");
                return null;
            }
            // 判断Key是否为16位
            if (sKey.length() != 16) {
                System.out.print("Key长度不是16位");
                return null;
            }
            byte[] raw = sKey.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] encrypted1 = new BASE64Decoder().decodeBuffer(sSrc);
            try {
                byte[] original = cipher.doFinal(encrypted1);
                String originalString = new String(original, "utf-8");
                return originalString;
            } catch (Exception e) {
                System.out.println(e.toString());
                return null;
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return null;
        }
    }

    //{0}：车牌号码,{1}：接口URI,{2}：toke,{3}：md5字符串
    public static class UserAgentInfo {

        private String carNum;
        private String uri;
        private String token;
        private String md5;

        public String getCarNum() {
            return carNum;
        }

        public void setCarNum(String carNum) {
            this.carNum = carNum;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getMd5() {
            return md5;
        }

        public void setMd5(String md5) {
            this.md5 = md5;
        }

        public static UserAgentInfo of(String userAgent) {
            String[] split = userAgent.split("_");
            UserAgentInfo info = new UserAgentInfo();
            info.setCarNum(split[0]);
            info.setUri(split[1]);
            info.setToken(split[2]);
            String md5 = split[3];
            int index = Integer.valueOf(md5.substring(md5.length() - 1));
            String encodeRules = md5.substring(index, index + 16);
            info.setMd5(encodeRules);
            return info;
        }
    }

    public static class CommParam {

        public String appVer; //string    [20] （收费系统应用软件版本号）
        public String flag; //应用版本号   string  [20] （收费系统应用软件版本号）
        public String parkNum; //车场账号    string  [20] （必填）（系统为每个停车场分配的编号）该编号对应的是，场区配置中，扩展配置下的“扩展编号”
        public String psam; //PSAM卡号  string  [12] 可不填
        public String sim; //SIM卡号   string  [20]
        public String sysVer; //系统版本号   string  [20] （操作系统版本号）
        public String tsn; //终端序列号   string  [16] （数据采集器的硬件编号）

        public String getAppVer() {
            return appVer;
        }

        public void setAppVer(String appVer) {
            this.appVer = appVer;
        }

        public String getFlag() {
            return flag;
        }

        public void setFlag(String flag) {
            this.flag = flag;
        }

        public String getParkNum() {
            return parkNum;
        }

        public void setParkNum(String parkNum) {
            this.parkNum = parkNum;
        }

        public String getPsam() {
            return psam;
        }

        public void setPsam(String psam) {
            this.psam = psam;
        }

        public String getSim() {
            return sim;
        }

        public void setSim(String sim) {
            this.sim = sim;
        }

        public String getSysVer() {
            return sysVer;
        }

        public void setSysVer(String sysVer) {
            this.sysVer = sysVer;
        }

        public String getTsn() {
            return tsn;
        }

        public void setTsn(String tsn) {
            this.tsn = tsn;
        }

        @Override
        public String toString() {
            return "CommParam [appVer=" + appVer + ", flag=" + flag + ", parkNum=" + parkNum + ", psam=" + psam + ", sim=" + sim + ", sysVer="
                    + sysVer + ", tsn=" + tsn + "]";
        }
    }

    public static class CarPreArriveInfo {

        private String actTime;//操作时间    string  [20] （必填）（格式：yyyyMMddHHmmss）
        private String actType;//操作类型    string  [4] （必填）（0代表月租长包车辆， 1代表时租访客车辆， 2代表免费车辆， 3代表异常未知车辆）
        private String addBerth;//附加泊位    string  [16]
        private String berthId;//泊位编号    string  [16]
        private String bizSn;//业务流水号   string  [20] （预进场数据ID）
        private String businessType;//业务类型    string  [4] （必填）（1-进场，2-出场）
        private String carNum;//车牌号 string  [16] （必填）
        private String carType;//车辆类型    string  [4] （必填）（1-小型车，2-大型车）
        private CommParam commParam;//通用字段    object  包含通用字段中的所有字段
        private String guestRemainNum;//访客剩余车位  string  [8] （必填）
        private String monthlyCertNumber;//包月证号    string  [32]
        private String monthlyRemainNum;//月租剩余车位  string  [8] （必填）
        private String preArriveTime;//预进场时间   string  [20] （必填）（格式：yyyyMMddHHmmss）
        private String uid;//工号  string  [12] （停车场端收费管理系统的登录工号）
        private String voucherNo;//停车凭证号   string  [20]
        private String voucherType;//停车凭证类型  string  [4] （1、交通卡 2、银联卡 3、第三方支付 51、VIP卡号 101、临时车卡号 102、临时车票号） 响应参数列表

        public String getActTime() {
            return actTime;
        }

        public void setActTime(String actTime) {
            this.actTime = actTime;
        }

        public String getActType() {
            return actType;
        }

        public void setActType(String actType) {
            this.actType = actType;
        }

        public String getAddBerth() {
            return addBerth;
        }

        public void setAddBerth(String addBerth) {
            this.addBerth = addBerth;
        }

        public String getBerthId() {
            return berthId;
        }

        public void setBerthId(String berthId) {
            this.berthId = berthId;
        }

        public String getBizSn() {
            return bizSn;
        }

        public void setBizSn(String bizSn) {
            this.bizSn = bizSn;
        }

        public String getBusinessType() {
            return businessType;
        }

        public void setBusinessType(String businessType) {
            this.businessType = businessType;
        }

        public String getCarNum() {
            return carNum;
        }

        public void setCarNum(String carNum) {
            this.carNum = carNum;
        }

        public String getCarType() {
            return carType;
        }

        public void setCarType(String carType) {
            this.carType = carType;
        }

        public CommParam getCommParam() {
            return commParam;
        }

        public void setCommParam(CommParam commParam) {
            this.commParam = commParam;
        }

        public String getGuestRemainNum() {
            return guestRemainNum;
        }

        public void setGuestRemainNum(String guestRemainNum) {
            this.guestRemainNum = guestRemainNum;
        }

        public String getMonthlyCertNumber() {
            return monthlyCertNumber;
        }

        public void setMonthlyCertNumber(String monthlyCertNumber) {
            this.monthlyCertNumber = monthlyCertNumber;
        }

        public String getMonthlyRemainNum() {
            return monthlyRemainNum;
        }

        public void setMonthlyRemainNum(String monthlyRemainNum) {
            this.monthlyRemainNum = monthlyRemainNum;
        }

        public String getPreArriveTime() {
            return preArriveTime;
        }

        public void setPreArriveTime(String preArriveTime) {
            this.preArriveTime = preArriveTime;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getVoucherNo() {
            return voucherNo;
        }

        public void setVoucherNo(String voucherNo) {
            this.voucherNo = voucherNo;
        }

        public String getVoucherType() {
            return voucherType;
        }

        public void setVoucherType(String voucherType) {
            this.voucherType = voucherType;
        }

        @Override
        public String toString() {
            return "CarPreArriveInfo [actTime=" + actTime + ", actType=" + actType + ", addBerth=" + addBerth + ", berthId=" + berthId + ", bizSn="
                    + bizSn + ", businessType=" + businessType + ", carNum=" + carNum + ", carType=" + carType + ", commParam=" + commParam
                    + ", guestRemainNum=" + guestRemainNum + ", monthlyCertNumber=" + monthlyCertNumber + ", monthlyRemainNum=" + monthlyRemainNum
                    + ", preArriveTime=" + preArriveTime + ", uid=" + uid + ", voucherNo=" + voucherNo + ", voucherType=" + voucherType + "]";
        }
    }

    public static class ResponseData {

        private String isOpen;//是否线上控制开闸    string  1：线上控制开闸，2：线上控制不开闸（欠费用户），3：线下控制处理

        public String getIsOpen() {
            return isOpen;
        }

        public void setIsOpen(String isOpen) {
            this.isOpen = isOpen;
        }
    }

    public static class CarPreArriveResponse {

        public static final String SUCCESS_CODE = "000000";
        public static final String FAILURE_CODE = "111111";
        public static final String SUCCESS_MSG = "success";
        public static final String FAILURE_MSG = "failure";

        private ResponseData data;
        private String resCode;
        private String resMsg;

        public static CarPreArriveResponse newSuccessInstane() {
            CarPreArriveResponse response = new CarPreArriveResponse();
            ResponseData data = new ResponseData();
            data.setIsOpen("1");
            response.setData(data);
            response.setResCode(SUCCESS_CODE);
            response.setResMsg(SUCCESS_MSG);
            return response;
        }

        public static CarPreArriveResponse newFailureInstane(String resMsg) {
            CarPreArriveResponse response = new CarPreArriveResponse();
            ResponseData data = new ResponseData();
            data.setIsOpen("2");
            response.setData(data);
            response.setResCode(FAILURE_CODE);
            response.setResMsg(resMsg);
            return response;
        }

        public ResponseData getData() {
            return data;
        }

        public void setData(ResponseData data) {
            this.data = data;
        }

        public String getResCode() {
            return resCode;
        }

        public void setResCode(String resCode) {
            this.resCode = resCode;
        }

        public String getResMsg() {
            return resMsg;
        }

        public void setResMsg(String resMsg) {
            this.resMsg = resMsg;
        }
    }

    public class APIHttpClient {

        //接口地址
        private String apiURL = "";
        private Log logger = LogFactory.getLog(this.getClass());
        private static final String pattern = "yyyy-MM-dd HH:mm:ss:SSS";
        private CloseableHttpClient httpClient = null;
        private HttpPost method = null;
        private long startTime = 0L;
        private long endTime = 0L;
        private int status = 0;

        /**
         * 接口地址
         * 
         * @param url
         */
        public APIHttpClient(String url) {
            if (url != null) {
                this.apiURL = url;
            }
            if (apiURL != null) {
                httpClient = HttpClients.createDefault();
                //配置超时时间
                RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(1000).setConnectionRequestTimeout(1000).setSocketTimeout(1000)
                        .setRedirectsEnabled(true).build();
                method = new HttpPost(apiURL);
                method.setConfig(requestConfig);
                method.setHeader("User-Agent", "kun_/test/test_Nv6RRuGEVvmGjB+jimI/gw==_2aa97c6f8d231f2660d1ea162b38c2dc39e2a59d15c637030b3");
            }
        }

        /**
         * 调用 API
         * 
         * @param parameters
         * @return
         */
        public String post(String parameters) {
            String body = null;
            logger.info("parameters:" + parameters);

            if (method != null & parameters != null && !"".equals(parameters.trim())) {
                try {
                    StringEntity entity = new StringEntity(parameters);
                    entity.setContentEncoding("UTF-8");
                    entity.setContentType("application/json");//设置为 json数据
                    method.setEntity(entity);
                    //设置编码  
                    HttpResponse response = httpClient.execute(method);
                    endTime = System.currentTimeMillis();
                    int statusCode = response.getStatusLine().getStatusCode();
                    logger.info("statusCode:" + statusCode);
                    logger.info("调用API 花费时间(单位：毫秒)：" + (endTime - startTime));
                    if (statusCode != HttpStatus.SC_OK) {
                        logger.error("Method failed:" + response.getStatusLine());
                        status = 1;
                    }

                    //Read the response body
                    body = EntityUtils.toString(response.getEntity());

                } catch (IOException e) {
                    //网络错误
                    status = 3;
                } finally {
                    logger.info("调用接口状态：" + status);
                }

            }
            return body;
        }

        /**
         * 0.成功 1.执行方法失败 2.协议错误 3.网络错误
         * 
         * @return the status
         */
        public int getStatus() {
            return status;
        }

        /**
         * @param status the status to set
         */
        public void setStatus(int status) {
            this.status = status;
        }

        /**
         * @return the startTime
         */
        public long getStartTime() {
            return startTime;
        }

        /**
         * @return the endTime
         */
        public long getEndTime() {
            return endTime;
        }
    }

    public static void main(String[] args) throws Exception {
        String md5 = "2aa97c6f8d231f2660d1ea162b38c2dc39e2a59d15c637030b3";
        int index = Integer.valueOf(md5.substring(md5.length() - 1));
        //04a6b913f49ed2c5
        String encodeRules = md5.substring(index, index + 16);
        String content = "3pahwLxnh9kMao+UcLU03vuADFR+PNNZI6GnLaEXVdQhPMpFb7l7bBvUScbS9uLOSsWeaD6x/xyqvZQpJRRGygWfL5Btb+xDMUUTzJElqsF/FvZ5jfeyXuokiJFEmmjUkG4npa/ybDjd/y2hR5XTX81+uYzpNdMOG1KJDeKsv1GgQE09da9aZlgImRybzYSXp+8K4Xza8EHnfcl9WGt0J4ssfRVWBO1XW1ASyjFlJE93nKd+gWjsZ5Cw0zoRtvmJldRrgTuaOxEJXL+ROvmjcy8Ktdi3bTGxzEpgtqotlNm/k7OaM5zwn2NUZeUOiIEXHDhjJWDpnYX4lgCXuzGRzwdk1qFVWNzGDppFFtKpldiTppf9MttHzwTSoAHsAXNvdql8MFWPpPMAoKv469X7tYW6IPZ12iCJCv8VqtOPET0LQrnXWzWLezWWMro/B8fI8p695CDP+M+atBfVzl5acw==";
        String result = SymmetricEncoder.Decrypt(content, encodeRules);
        System.err.println(result);
        CarPreArriveInfo info = JSON.parseObject(result, CarPreArriveInfo.class);
        System.err.println(info.toString());
        //        Group group2 = JSON.parseObject(jsonString, Group.class);

        CarPreArriveResponse newSuccessInstane = CarPreArriveResponse.newSuccessInstane();
        String response = JSON.toJSONString(newSuccessInstane);
        System.err.println(response);

    }
}


 
 
