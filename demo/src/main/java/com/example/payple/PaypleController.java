package com.example.payple;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PaypleController {


	/**
	 * 파트너 인증 메소드
	 * 
	 * @param params // 상황별 파트너 인증 요청 파라미터(계정정보 제외)
	 * @return JSONObject // 파트너 인증 응답값
	 */
	public JSONObject payAuth(Map<String, String> params) {
		JSONObject jsonObject = new JSONObject();
		JSONParser jsonParser = new JSONParser();
		try {

			// 파트너 인증 Request URL
			String pURL = "https://democpay.payple.kr/php/auth.php"; // TEST

			// 계정정보
			String cst_id = "test";
			String cust_key = "abcd1234567890";

			JSONObject obj = new JSONObject();
			obj.put("cst_id", cst_id);
			obj.put("custKey", cust_key);

			// 상황별 파트너 인증 요청 파라미터
			
			 if (params != null) { 
				 for (Map.Entry<String, String> elem : params.entrySet()) { 
					 obj.put(elem.getKey(), elem.getValue()); 
				 } 
			}
			 

			System.out.println("파트너 인증 Request: " + obj.toString());

			URL url = new URL(pURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			/*
			 * ※ Referer 설정 방법 
			 * TEST : referer에는 테스트 결제창을 띄우는 도메인을 넣어주셔야합니다. 
			 * 		  결제창을 띄울 도메인과 referer값이 다르면 무한로딩이 발생합니다. 
			 * REAL : referer에는 가맹점 도메인으로 등록된 도메인을 넣어주셔야합니다.
			 * 		  다른 도메인을 넣으시면 [AUTH0004] 에러가 발생합니다. 
			 * 		  또한, TEST에서와 마찬가지로 결제창을 띄우는 도메인과 같아야 합니다.
			 */
			con.setRequestMethod("POST");
			con.setRequestProperty("content-type", "application/json");
			con.setRequestProperty("charset", "UTF-8");
			con.setRequestProperty("referer", "http://localhost:8080");
			con.setDoOutput(true);

			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			//wr.writeBytes(obj.toString());
			wr.write(obj.toString().getBytes());
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
	

	/* Payple API - Routing */

	// 현금영수증 발행 (taxSaveReq.jsp)
	@RequestMapping(value = "/taxSaveReq")
	public String taxSaveReqRoute(Model model) {
		return "taxSaveReq";
	}
	
	// 등록 조회 및 해지(카드/계좌) (payUser.jsp)
	@RequestMapping(value = "/payUser")
	public String payUserRoute(Model model) {
		return "payUser";
	}

	/* Payple API - Request(POST) */



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
		JSONObject authObj = new JSONObject();
		authObj = payAuth(payUserInfoParams);

		// 파트너 인증 응답 값
		String cstId = (String) authObj.get("cst_id"); // 파트너사 ID
		String custKey = (String) authObj.get("custKey"); // 파트너사 키
		String authKey = (String) authObj.get("AuthKey"); // 인증 키
		String payUserInfoURL = (String) authObj.get("return_url"); // 현금영수 발행/취소 요청 URL
		
		// 요청 파라미터
		String payer_id = request.getParameter("PCD_PAYER_ID"); // (필수) 결제자 고유 ID (빌링키)
		
		try {
			// 링크URL 생성 요청 전송
			JSONObject payUserInfoObj = new JSONObject();
			
			payUserInfoObj.put("PCD_CST_ID", cstId);
			payUserInfoObj.put("PCD_CUST_KEY", custKey);
			payUserInfoObj.put("PCD_AUTH_KEY", authKey);
			payUserInfoObj.put("PCD_PAYER_ID", payer_id);
			
			URL url = new URL(payUserInfoURL);
			
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			
			con.setRequestMethod("POST");
			con.setRequestProperty("content-type", "application/json");
			con.setRequestProperty("referer", "http://localhost:8080");
			con.setDoOutput(true);
			
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.write(payUserInfoObj.toString().getBytes());			
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
		JSONObject authObj = new JSONObject();
		authObj = payAuth(payUserDelParams);

		// 파트너 인증 응답 값
		String cstId = (String) authObj.get("cst_id"); // 파트너사 ID
		String custKey = (String) authObj.get("custKey"); // 파트너사 키
		String authKey = (String) authObj.get("AuthKey"); // 인증 키
		String payUserDelURL = (String) authObj.get("return_url"); // 등록해지 요청 URL
		
		// 요청 파라미터
		String payer_id = request.getParameter("PCD_PAYER_ID"); // (필수) 결제자 고유 ID (빌링키)
		
		try {
			// 링크URL 생성 요청 전송
			JSONObject payUserDelObj = new JSONObject();
			
			payUserDelObj.put("PCD_CST_ID", cstId);
			payUserDelObj.put("PCD_CUST_KEY", custKey);
			payUserDelObj.put("PCD_AUTH_KEY", authKey);
			payUserDelObj.put("PCD_PAYER_ID", payer_id);
			
			URL url = new URL(payUserDelURL);
			
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			
			con.setRequestMethod("POST");
			con.setRequestProperty("content-type", "application/json");
			con.setRequestProperty("referer", "http://localhost:8080");
			con.setDoOutput(true);
			
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.write(payUserDelObj.toString().getBytes());			
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
