package com.example.payple.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.example.payple.common.AuthResponse;
import com.example.payple.common.PaypleAuthenticator;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class linkController {

	/*
	 * linkReg.jsp : URL링크결제 생성 페이지
	 */
	@RequestMapping(value = "/linkReg")
	public String linkRegRoute() {
		return "linkReg";
	}

	/*
	 *linkReg : URL링크결제 생성
	 */
	@ResponseBody
	@PostMapping(value = "/linkReg")
	public JSONObject linkReg(HttpServletRequest request) {
		JSONObject jsonObject = new JSONObject();
		JSONParser jsonParser = new JSONParser();

		// URL링크 생성 전 파트너 인증
		Map<String, String> linkParams = new HashMap<>();
		linkParams.put("PCD_PAY_WORK", "LINKREG");

		JSONObject authObj = PaypleAuthenticator.payAuth(linkParams);
		AuthResponse auth = AuthResponse.from(authObj);

		// 링크URL 생성 요청 파라미터
		String pay_work = "LINKREG";// (필수) 요청 작업 구분 (URL링크결제 : LINKREG)
		String pay_type = request.getParameter("PCD_PAY_TYPE"); // (필수) 결제수단 (transfer|card)
		String pay_goods = request.getParameter("PCD_PAY_GOODS"); // (필수) 상품명
		String pay_total = request.getParameter("PCD_PAY_TOTAL"); // (필수) 결제요청금액
		String card_ver = request.getParameter("PCD_CARD_VER"); // 카드 세부 결제방식 (Default: 01+02)
		String pay_istax = request.getParameter("PCD_PAY_ISTAX"); // 과세여부
		String pay_taxtotal = request.getParameter("PCD_PAY_TAXTOTAL"); // 부가세(복합과세 적용 시)
		String taxsave_flag = request.getParameter("PCD_TAXSAVE_FLAG"); // 현금영수증 발행요청 (Y|N)
		String link_expiredate = request.getParameter("PCD_LINK_EXPIREDATE"); // URL 결제 만료일

		try {
			// 링크URL 생성 요청 전송
			JSONObject linkRegObj = new JSONObject();

			linkRegObj.put("PCD_CST_ID", auth.getCstId());
			linkRegObj.put("PCD_CUST_KEY", auth.getCustKey());
			linkRegObj.put("PCD_AUTH_KEY", auth.getAuthKey());
			linkRegObj.put("PCD_PAY_WORK", pay_work);
			linkRegObj.put("PCD_PAY_TYPE", pay_type);
			linkRegObj.put("PCD_PAY_GOODS", pay_goods);
			linkRegObj.put("PCD_CARD_VER", card_ver);
			linkRegObj.put("PCD_PAY_TOTAL", pay_total);
			linkRegObj.put("PCD_PAY_ISTAX", pay_istax);
			linkRegObj.put("PCD_PAY_TAXTOTAL", pay_taxtotal);
			linkRegObj.put("PCD_TAXSAVE_FLAG", taxsave_flag);
			linkRegObj.put("PCD_LINK_EXPIREDATE", link_expiredate);

			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost httpPost = new HttpPost(auth.getReturnUrl());
			httpPost.setHeader("Content-Type", "application/json");
			httpPost.setHeader("Referer", "http://localhost:8080");
			httpPost.setEntity(new StringEntity(linkRegObj.toString(), "UTF-8"));

			HttpResponse response = httpClient.execute(httpPost);

			if (response.getStatusLine().getStatusCode() == 200) {
				ResponseHandler<String> handler = new BasicResponseHandler();
				String strResponse = handler.handleResponse(response);

				jsonObject = (JSONObject) jsonParser.parse(strResponse);
			} else {
				System.out.println("Http Response Error : [" + response.getStatusLine().getStatusCode() + "] " + response);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

}
