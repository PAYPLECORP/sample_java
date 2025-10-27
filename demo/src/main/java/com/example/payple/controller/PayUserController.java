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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PayUserController {
	
	/*
	 * payUser.jsp : 등록 조회 및 해지(카드/계좌) 
	 */
	@RequestMapping(value = "/payUser")
	public String payUserRoute(Model model) {
		return "payUser";
	}
	/*
	 * payUserInfo : 등록 조회
	 */

	@ResponseBody
	@PostMapping(value = "/payUserInfo")
	public JSONObject payUserInfo(HttpServletRequest request) {
		JSONObject jsonObject = new JSONObject();
		JSONParser jsonParser = new JSONParser();
		// 파트너 인증
		Map<String, String> payUserInfoParams = new HashMap<>();

		// (필수) 요청 작업 구분
		payUserInfoParams.put("PCD_PAY_WORK", "PUSERINFO");

		JSONObject authObj = PaypleAuthenticator.payAuth(payUserInfoParams);
		AuthResponse auth = AuthResponse.from(authObj);

		// 요청 파라미터
		String payer_id = request.getParameter("PCD_PAYER_ID"); // (필수) 결제자 고유 ID (빌링키)

		try {
			// 등록 조회 요청 전송
			JSONObject payUserInfoObj = HttpUtil.createBasePaypleRequest(
				auth.getCstId(), auth.getCustKey(), auth.getAuthKey());
			payUserInfoObj.put("PCD_PAYER_ID", payer_id);

			HttpURLConnection con = HttpUtil.createPaypleConnection(auth.getReturnUrl());
			HttpUtil.sendJsonRequest(con, payUserInfoObj);

			String responseBody = HttpUtil.readResponse(con);
			jsonObject = (JSONObject) jsonParser.parse(responseBody);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

	/*
	 * payUserDel : 등록 해지
	 */
	@ResponseBody
	@PostMapping(value = "/payUserDel")
	public JSONObject payUserDel(HttpServletRequest request) {
		JSONObject jsonObject = new JSONObject();
		JSONParser jsonParser = new JSONParser();
		// 파트너 인증
		Map<String, String> payUserDelParams = new HashMap<>();

		// (필수) 요청 작업 구분
		payUserDelParams.put("PCD_PAY_WORK", "PUSERDEL");

		JSONObject authObj = PaypleAuthenticator.payAuth(payUserDelParams);
		AuthResponse auth = AuthResponse.from(authObj);

		// 요청 파라미터
		String payer_id = request.getParameter("PCD_PAYER_ID"); // (필수) 결제자 고유 ID (빌링키)

		try {
			// 등록 해지 요청 전송
			JSONObject payUserDelObj = HttpUtil.createBasePaypleRequest(
				auth.getCstId(), auth.getCustKey(), auth.getAuthKey());
			payUserDelObj.put("PCD_PAYER_ID", payer_id);

			HttpURLConnection con = HttpUtil.createPaypleConnection(auth.getReturnUrl());
			HttpUtil.sendJsonRequest(con, payUserDelObj);

			String responseBody = HttpUtil.readResponse(con);
			jsonObject = (JSONObject) jsonParser.parse(responseBody);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

}
