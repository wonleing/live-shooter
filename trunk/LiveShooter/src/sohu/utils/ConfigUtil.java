package sohu.utils;

/**
 * 获取配置文件
 * @author bywyu
 *
 */
public  class ConfigUtil {
	
	private static ConfigUtil instance;
	
	//Activity之间传递值时的KEY
	public static final String OAUTH_VERIFIER_URL = "oauth_verifier_url";
	
	private String curWeibo = "";
	private String appKey = "";
	private String appSecret = "";
	private String request_token_url = "";
	private String authoriz_token_url = "";
	private String access_token_url = ""; 
	
	public static final String SOHUW = "sohu";

	public static String callBackUrl = "http://api.t.sohu.com/oauth/oob?";
	//--------------------sohu
	//API Key btBPDWJxYfTt0cGUzazy
	public static String sohu_AppKey = "LDXuq1duUOCz3k8qfwMY";//"wYwE7n60SVImdy7S32ub";//"btBPDWJxYfTt0cGUzazy";
	public static String sohu_AppSecret = "w(BTXdm6VoG4YtY3O8Ch)yxedM26LJUtfJ!C=twj";//"2$hcGL*jX=3dfJqqO9EAPGG7P#2xjfVDDxe^aKr(";//"rruL^5)AvXs0CAvcp!KdL49r#bS6Zg9UQI)-%l6m";
	public static String sohu_Request_token_url = "http://api.t.sohu.com/oauth/request_token";
	public static String sohu_Authoriz_token_url = "http://api.t.sohu.com/oauth/authorize";
	public static String sohu_Access_token_url = "http://api.t.sohu.com/oauth/access_token";
   
	public static synchronized ConfigUtil getInstance() {
        if (instance == null) {
            instance = new ConfigUtil();
        }
        return instance;
    }
	
	public ConfigUtil(){
		initSohuData();
	}
	
	
	/**
	 * 初始化SOHU认证信息
	 */
	public void initSohuData() {
		setAppKey(sohu_AppKey);
		setAppSecret(sohu_AppSecret);
		setRequest_token_url(sohu_Request_token_url);
		setAuthoriz_token_url(sohu_Authoriz_token_url);
		setAccess_token_url(sohu_Access_token_url);
    }
	
	
	public String getCurWeibo() {
    	return curWeibo;
    }
	
	/**
	 * 设置当前操作的weibo
	 * 		不同的weibo请求存在着差异
	 * @param curWeibo
	 */
	public void setCurWeibo(String curWeibo) {
    	this.curWeibo = curWeibo;
    }
	
	public String getAppKey() {
    	return appKey;
    }

	public void setAppKey(String appKey) {
    	this.appKey = appKey;
    }

	public String getAppSecret() {
    	return appSecret;
    }

	public void setAppSecret(String appSecret) {
    	this.appSecret = appSecret;
    }

	public String getRequest_token_url() {
    	return request_token_url;
    }

	public void setRequest_token_url(String requestTokenUrl) {
    	request_token_url = requestTokenUrl;
    }
	
	public String getAuthoriz_token_url() {
    	return authoriz_token_url;
    }

	public void setAuthoriz_token_url(String authorizTokenUrl) {
    	authoriz_token_url = authorizTokenUrl;
    }

	public String getAccess_token_url() {
    	return access_token_url;
    }

	public void setAccess_token_url(String accessTokenUrl) {
    	access_token_url = accessTokenUrl;
    }
}
