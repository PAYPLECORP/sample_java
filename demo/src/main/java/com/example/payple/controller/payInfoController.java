package com.example.payple.controller;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.example.payple.common.AuthResponse;
import com.example.payple.common.HttpUtil;
import com.example.payple.common.PaypleAuthenticator;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class payInfoController {

	/*
	 * payInfo.jsp : 결제결과 조회 페이지
	 */
	@RequestMapping(value = "/payInfo")
	public String payInfoRoute() {
		return "payInfo";
	}

	/*
	 * payInfo : 결제결과 조회
	 */
	@ResponseBody
	@PostMapping(value = "/payInfo")
	public JSONObject payInfo(HttpServletRequest request) {
		JSONObject jsonObject = new JSONObject();
		JSONParser jsonParser = new JSONParser();

		// 결제결과 조회 전 파트너 인증
		Map<String, String> infoParams = new HashMap<>();
		infoParams.put("PCD_PAYCHK_FLAG", "Y");

		JSONObject authObj = PaypleAuthenticator.payAuth(infoParams);
		AuthResponse auth = AuthResponse.from(authObj);

		// 결제결과 조회 요청 파라미터
		String pay_type = request.getParameter("PCD_PAY_TYPE"); // (필수) 결제수단 (transfer|card)
		String pay_oid = request.getParameter("PCD_PAY_OID"); // (필수) 주문번호
		String pay_date = request.getParameter("PCD_PAY_DATE"); // (필수) 원거래 결제일자

		try {
			// 결제결과 조회 요청 전송
			JSONObject payInfoObj = HttpUtil.createBasePaypleRequest(
				auth.getCstId(), auth.getCustKey(), auth.getAuthKey());
			payInfoObj.put("PCD_PAYCHK_FLAG", "Y");
			payInfoObj.put("PCD_PAY_TYPE", pay_type);
			payInfoObj.put("PCD_PAY_OID", pay_oid);
			payInfoObj.put("PCD_PAY_DATE", pay_date);

			HttpURLConnection con = HttpUtil.createPaypleConnection(auth.getReturnUrl());
			HttpUtil.sendJsonRequest(con, payInfoObj);

			String responseBody = HttpUtil.readResponse(con);
			jsonObject = (JSONObject) jsonParser.parse(responseBody);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

}
