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
public class taxSaveController {
	
	/*
	 *  taxSaveReq.jsp : 현금영수증 발행
	 */
	@RequestMapping(value = "/taxSaveReq")
	public String taxSaveReqRoute(Model model) {
		return "taxSaveReq";
	}
	
	/*
	 * taxSaveReq : 현금영수증 발행 및 취소
	 */
	@ResponseBody
	@PostMapping(value = "/taxSaveReq")
	public JSONObject taxSaveReq(HttpServletRequest request) {
		JSONObject jsonObject = new JSONObject();
		JSONParser jsonParser = new JSONParser();
		// 파트너 인증
		Map<String, String> taxSaveParams = new HashMap<>();

		// (필수) 요청 작업 구분 (현금영수증발행 : TSREG, 현금영수증발행취소 : TSCANCEL)
		String pay_work = request.getParameter("PCD_TAXSAVE_REQUEST").equals("regist") ? "TSREG" : "TSCANCEL";
		taxSaveParams.put("PCD_PAY_WORK", pay_work);

		JSONObject authObj = PaypleAuthenticator.payAuth(taxSaveParams);
		AuthResponse auth = AuthResponse.from(authObj);
		System.out.println(auth.getReturnUrl());
		
		// 요청 파라미터
		String payer_id = request.getParameter("PCD_PAYER_ID"); // (필수) 결제자 고유 ID (빌링키)
		String pay_oid = request.getParameter("PCD_PAY_OID"); // (필수) 주문번호
		String taxsave_amount = request.getParameter("PCD_PAY_TOTAL"); // (필수) 현금영수증 발행금액
		String taxsave_tradeuse = request.getParameter("PCD_PAY_ISTAX"); // 현금영수증 발행 타입 (personal:소득공제용 | company:지출증빙)
		String taxsave_identinum = request.getParameter("PCD_PAY_TAXTOTAL"); // 현금영수증 발행대상 번호
		
		
		try {
			// 현금영수증 발행/취소 요청 전송
			JSONObject taxSaveReqObj = HttpUtil.createBasePaypleRequest(
				auth.getCstId(), auth.getCustKey(), auth.getAuthKey());
			taxSaveReqObj.put("PCD_PAYER_ID", payer_id);
			taxSaveReqObj.put("PCD_PAY_OID", pay_oid);
			taxSaveReqObj.put("PCD_TAXSAVE_AMOUNT", taxsave_amount);
			taxSaveReqObj.put("PCD_TAXSAVE_TRADEUSE", taxsave_tradeuse);
			taxSaveReqObj.put("PCD_TAXSAVE_IDENTINUM", taxsave_identinum);

			HttpURLConnection con = HttpUtil.createPaypleConnection(auth.getReturnUrl());
			HttpUtil.sendJsonRequest(con, taxSaveReqObj);

			String responseBody = HttpUtil.readResponse(con);
			jsonObject = (JSONObject) jsonParser.parse(responseBody);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObject;
	}		
}
