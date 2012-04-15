/*
Copyright (c) 2007-2009, Yusuke Yamamoto
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
 * Neither the name of the Yusuke Yamamoto nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Yusuke Yamamoto ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Yusuke Yamamoto BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package sohu;

import java.io.File;
import java.io.IOException;

import sina.Comment;
import sina.WeiboException;
import sina.util.URLEncodeUtils;
import sohu.data.OAuth;
import sohu.http.AccessToken;
import sohu.http.HttpClient;
import sohu.http.PostParameter;
import sohu.http.Response;

import com.android.liveshooter.common.AppApiPreference;


/**
 * A java reporesentation of the <a href="http://open.t.sina.com.cn/wiki/">Weibo API</a>
 * @editor sinaWeibo
 */
/**
 * @author sinaWeibo
 *
 */

public class SohuBlog implements java.io.Serializable {
	private static final long serialVersionUID = -1486360080128882436L;
	private HttpClient http;
	
	public SohuBlog() {
		this.http = new HttpClient();
	}
	
	public void setAccesTokenAndSecret(String token,String secret){
		this.http.setOAuthAccessToken(new AccessToken(token, secret));
		this.http.setOAuthConsumer(AppApiPreference.SOHO_APP_KEY, AppApiPreference.SOHO_APP_KEY_SECRET);
	}
	

	/**
	 * 验证当前用户身份是否合法
	 * @return user
	 * @throws Exception 
	 * @since Weibo4J 1.2.1
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Account/verify_credentials">account/verify_credentials </a>
	 */
	public String verifyCredentials() throws Exception {
		/*return new User(get(getBaseURL() + "account/verify_credentials.xml"
                , true), this);*/
		OAuth auth = OAuth.getInstance();
		auth.setKeyAndSecret(AppApiPreference.SOHO_APP_KEY, AppApiPreference.SOHO_APP_KEY_SECRET);
		return get("http://api.t.sohu.com/account/verify_credentials.json", true).getResponseContent();
	}
	

	/**
	 * Issues an HTTP GET request.
	 *
	 * @param url          the request url
	 * @param authenticate if true, the request will be sent with BASIC authentication header
	 * @return the response
	 * @throws WeiboException when Weibo service or network is unavailable
	 */

	private Response get(String url, boolean authenticate) throws Exception {
		return get(url, null, authenticate);
	}
	
	/**
	 * Issues an HTTP GET request.
	 *
	 * @param url          the request url
	 * @param params       the request parameters
	 * @param authenticate if true, the request will be sent with BASIC authentication header
	 * @return the response
	 * @throws WeiboException when Weibo service or network is unavailable
	 */
	protected Response get(String url, PostParameter[] params, boolean authenticate) throws Exception {
		
		if (null != params && params.length > 0) {
			String encodedParams = HttpClient.encodeParameters(params);
			if (-1 == url.indexOf("?")) {
				url += "?" + encodedParams;
			} else {
				url += "&" + encodedParams;
			}
		}
		return http.get(url, authenticate);
	}
	
	/**
	 * 获取request token
	 * @return generated request token.
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @since Weibo4J 1.2.1
	 * @see <a href="http://oauth.net/core/1.0/#auth_step1">OAuth Core 1.0 - 6.1.  Obtaining an Unauthorized Request Token</a>
	 */
	public sohu.http.RequestToken getOAuthRequestToken() throws WeiboException {
		return http.getOAuthRequestToken();
	}

	public sohu.http.RequestToken getOAuthRequestToken(String callback_url) throws WeiboException {
		return http.getOauthRequestToken(callback_url);
	}
	
	/**
	 * 发布一条微博信息
	 * @param status 要发布的微博消息文本内容
	 * @return the latest status
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @throws IOException 
	 * @throws OAuthCommunicationException 
	 * @throws OAuthExpectationFailedException 
	 * @throws OAuthMessageSignerException 
	 * @since Weibo4J 1.2.1
	 * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/update">statuses/update </a>
	 */
	public Response updateStatus(String status) throws WeiboException{
		status=URLEncodeUtils.encodeURL(status);
		return http.post("http://api.t.sohu.com/statuses/update.json",new PostParameter[]{new PostParameter("status",status)}, true);
		
	}
	

	/**
	 * 发表图片微博消息。目前上传图片大小限制为<5M。
	 * @param status 要发布的微博消息文本内容
	 * @param file 要上传的图片
	 * @return
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @since Weibo4J 1.2.1
	 * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/upload">statuses/upload </a>
	 */
	public Response uploadStatus(String status,File file) throws WeiboException {
		status=URLEncodeUtils.encodeURL(status);
		return http.multPartURL("pic","http://api.t.sohu.com/statuses/upload.json",new PostParameter[]{new PostParameter("status", status), new PostParameter("source", 1)},file, true);
	}
	
	/**
	 *对一条微博信息进行评论
	 * @param 评论内容。必须做URLEncode,信息内容不超过140个汉字。
	 * @param id 要评论的微博消息ID
	 * </br>如果提供了正确的cid参数，则该接口的表现为回复指定的评论。<br/>此时id参数将被忽略。<br/>即使cid参数代表的评论不属于id参数代表的微博消息，通过该接口发表的评论信息直接回复cid代表的评论。
	 * @return the comment object
	 * @throws WeiboException
	 * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/comment">Statuses/comment</a>
	 */
	public Response updateComment(String comment, String id) throws WeiboException {
		PostParameter[] params = null;
			params = new PostParameter[] {
				new PostParameter("comment", comment),
				new PostParameter("id", id)
		};
		return http.post("http://api.t.sohu.com/statuses/comment.json", params, true);
	}



}