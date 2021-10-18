package com.example.payple;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class taxSaveController extends PaypleController {
	
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
		JSONObject authObj = new JSONObject();
		authObj = payAuth(taxSaveParams);

		// 파트너 인증 응답 값
		String cstId = (String) authObj.get("cst_id"); // 파트너사 ID
		String custKey = (String) authObj.get("custKey"); // 파트너사 키
		String authKey = (String) authObj.get("AuthKey"); // 인증 키
		String taxSaveRegURL = (String) authObj.get("return_url"); // 현금영수 발행/취소 요청 URL
		System.out.println(taxSaveRegURL);
		
		// 요청 파라미터
		String payer_id = request.getParameter("PCD_PAYER_ID"); // (필수) 결제자 고유 ID (빌링키)
		String pay_oid = request.getParameter("PCD_PAY_OID"); // (필수) 주문번호
		String taxsave_amount = request.getParameter("PCD_PAY_TOTAL"); // (필수) 현금영수증 발행금액
		String taxsave_tradeuse = request.getParameter("PCD_PAY_ISTAX"); // 현금영수증 발행 타입 (personal:소득공제용 | company:지출증빙)
		String taxsave_identinum = request.getParameter("PCD_PAY_TAXTOTAL"); // 현금영수증 발행대상 번호
		
		
		try {
			// 링크URL 생성 요청 전송
			JSONObject taxsaveReqObj = new JSONObject();
			
			taxsaveReqObj.put("PCD_CST_ID", cstId);
			taxsaveReqObj.put("PCD_CUST_KEY", custKey);
			taxsaveReqObj.put("PCD_AUTH_KEY", authKey);
			taxsaveReqObj.put("PCD_PAYER_ID", payer_id);
			taxsaveReqObj.put("PCD_PAY_OID", pay_oid);
			taxsaveReqObj.put("PCD_TAXSAVE_AMOUNT", taxsave_amount);
			taxsaveReqObj.put("PCD_TAXSAVE_TRADEUSE", taxsave_tradeuse);
			taxsaveReqObj.put("PCD_TAXSAVE_IDENTINUM", taxsave_identinum);
			
			URL url = new URL(taxSaveRegURL);
			
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			
			con.setRequestMethod("POST");
			con.setRequestProperty("content-type", "application/json");
			con.setRequestProperty("referer", "http://localhost:8080");
			con.setDoOutput(true);
			
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.write(taxsaveReqObj.toString().getBytes());			
			wr.flush();
			wr.close();
			
			int responseCode = con.getResponseCode();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			jsonObject = (JSONObject) jsonParser.parse(response.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObject;
	}		
}
