package tencent.api;

import java.util.ArrayList;
import java.util.List;

import tencent.beans.*;

public class Tag_API extends RequestAPI{
	

	/**
	 * 添加标签
	 * @param oauth
	 * @param format 数据返回的格式（json或XML）
	 * @param tag 标签内容
	 * @return
	 * @throws Exception
	 */
	public String add(OAuth oauth, String format,
			String tag) throws Exception {
		List<QParameter> parameters = new ArrayList<QParameter>();
		parameters.add(new QParameter("format", format));
		parameters.add(new QParameter("tag", tag));

		return postContent("http://open.t.qq.com/api/tag/add",
				parameters, oauth);
	}
	
	/**
	 * 删除标签
	 * @param oauth
	 * @param format 数据返回的格式（json或XML）
	 * @param tagid  标签ID
	 * @return
	 * @throws Exception
	 */
	public String del(OAuth oauth, String format,
			String tagid) throws Exception {
		List<QParameter> parameters = new ArrayList<QParameter>();
		parameters.add(new QParameter("format", format));
		parameters.add(new QParameter("tagid", tagid));

		return postContent("http://open.t.qq.com/api/tag/del",
				parameters, oauth);
	}
}
