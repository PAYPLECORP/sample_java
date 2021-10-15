package com.example.demo;

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

	/*
	 * order.jsp : 주문 페이지
	 */
	@RequestMapping(value = "/order")
	public String order(Model model) {
		model.addAttribute("payer_no", "1234"); // 파트너 회원 고유번호
		model.addAttribute("payer_name", "홍길동"); // 결제자 이름
		model.addAttribute("payer_hp", "01012345678"); // 결제자 휴대전화번호
		model.addAttribute("payer_email", "test@payple.kr"); // 결제자 이메일
		model.addAttribute("pay_goods", "휴대폰"); // 상품명
		model.addAttribute("pay_total", "1000"); // 결제요청금액

		return "order";
	}

	/*
	 * order_confirm.jsp : 결제확인 페이지
	 */
	@RequestMapping(value = "/order_confirm")
	public String order_confirm(HttpServletRequest request, Model model) {

		model.addAttribute("pay_type", request.getParameter("pay_type")); // 결제수단 (transfer|card)
		model.addAttribute("pay_work", request.getParameter("pay_work")); // 결제요청 방식 (AUTH | PAY | CERT)
		model.addAttribute("payer_id", request.getParameter("payer_id")); // 결제자 고유 ID (빌링키)
		model.addAttribute("payer_no", request.getParameter("payer_no")); // 파트너 회원 고유번호
		model.addAttribute("payer_name", request.getParameter("payer_name")); // 결제자 이름
		model.addAttribute("payer_hp", request.getParameter("payer_hp")); // 결제자 휴대전화번호
		model.addAttribute("payer_email", request.getParameter("payer_email")); // 결제자 이메일
		model.addAttribute("pay_goods", request.getParameter("pay_goods")); // 상품명
		model.addAttribute("pay_total", request.getParameter("pay_total")); // 결제요청금액
		model.addAttribute("pay_taxtotal", request.getParameter("pay_taxtotal")); // 부가세(복합과세 적용 시)
		model.addAttribute("pay_istax", request.getParameter("pay_istax")); // 과세 여부 (과세:Y 비과세:N)
		model.addAttribute("pay_oid", request.getParameter("pay_oid")); // 주문번호
		model.addAttribute("taxsave_flag", request.getParameter("taxsave_flag")); // 현금영수증 발행요청 (Y|N)
		model.addAttribute("simple_flag", request.getParameter("simple_flag")); // 간편결제 여부 (Y|N)
		model.addAttribute("card_ver", request.getParameter("card_ver")); // 카드 세부 결제방식
		model.addAttribute("payer_authtype", request.getParameter("payer_authtype")); // 비밀번호 결제 인증방식 (pwd : 패스워드 인증)
		model.addAttribute("is_direct", request.getParameter("is_direct")); // 결제창 호출 방식 (DIRECT: Y | POPUP: N)
		model.addAttribute("hostname", System.getenv("HOSTNAME"));

		// 파트너 인증
		JSONObject obj = new JSONObject();
		obj = payAuth(null);

		// 파트너 인증 후 결제요청 시 필요한 필수 파라미터
		model.addAttribute("authKey", obj.get("AuthKey")); // 인증 키
		model.addAttribute("payReqURL", obj.get("return_url")); // 결제요청 URL

		return "order_confirm";
	}

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

	/*
	 * order_result.jsp : 결제결과 확인 페이지
	 */
	@RequestMapping(value = "/order_result")
	public String order_result(HttpServletRequest request, Model model) {

		// 1. 결제결과 모두 출력
		Enumeration<String> params = request.getParameterNames();
		String result = "";

		while (params.hasMoreElements()) {
			String name = (String) params.nextElement();
			result += name + " => " + request.getParameter(name) + "<br>";
		}
		model.addAttribute("result", result);

		// 2. 결제결과 파라미터로 받기 - 응답 파라미터를 받아서 활용해보세요.
		model.addAttribute("pay_rst", request.getParameter("PCD_PAY_RST")); // 결제요청 결과 (success | error)
		model.addAttribute("pay_code", request.getParameter("PCD_PAY_CODE")); // 결제요청 결과 코드
		model.addAttribute("pay_msg", request.getParameter("PCD_PAY_MSG")); // 결제요청 결과 메세지
		model.addAttribute("pay_type", request.getParameter("PCD_PAY_TYPE")); // 결제수단 (transfer|card)
		model.addAttribute("card_ver", request.getParameter("PCD_CARD_VER")); // 카드 세부 결제방식
		model.addAttribute("pay_work", request.getParameter("PCD_PAY_WORK")); // 결제요청 방식 (AUTH | PAY | CERT)
		model.addAttribute("auth_key", request.getParameter("PCD_AUTH_KEY")); // 결제요청 파트너 인증 토큰 값
		model.addAttribute("pay_reqkey", request.getParameter("PCD_PAY_REQKEY")); // (CERT방식) 최종 결제요청 승인키
		model.addAttribute("pay_cofurl", request.getParameter("PCD_PAY_COFURL")); // (CERT방식) 최종 결제요청 URL

		model.addAttribute("payer_id", request.getParameter("PCD_PAYER_ID")); // 결제자 고유 ID (빌링키)
		model.addAttribute("payer_no", request.getParameter("PCD_PAYER_NO")); // 결제자 고유번호 (파트너사 회원 회원번호)
		model.addAttribute("payer_name", request.getParameter("PCD_PAYER_NAME")); // 결제자 이름
		model.addAttribute("payer_hp", request.getParameter("PCD_PAYER_HP")); // 결제자 휴대전화번호
		model.addAttribute("payer_email", request.getParameter("PCD_PAYER_EMAIL")); // 결제자 이메일 (출금결과 수신)
		model.addAttribute("pay_oid", request.getParameter("PCD_PAY_OID")); // 주문번호
		model.addAttribute("pay_goods", request.getParameter("PCD_PAY_GOODS")); // 상품명
		model.addAttribute("pay_total", request.getParameter("PCD_PAY_TOTAL")); // 결제요청금액
		model.addAttribute("pay_taxtotal", request.getParameter("PCD_PAY_TAXTOTAL")); // 부가세(복합과세 적용 시)
		model.addAttribute("pay_istax", request.getParameter("PCD_PAY_ISTAX")); // 과세 여부 (과세:Y 비과세:N)
		model.addAttribute("pay_date", request.getParameter("PCD_PAY_TIME") == null ? "": request.getParameter("PCD_PAY_TIME").substring(0, 8)); // 결제완료 일자
		model.addAttribute("pay_bankacctype", request.getParameter("PCD_PAY_BANKACCTYPE")); // 고객 구분 (법인 | 개인 or 개인사업자)

		model.addAttribute("pay_bank", request.getParameter("PCD_PAY_BANK")); // 은행코드
		model.addAttribute("pay_bankname", request.getParameter("PCD_PAY_BANKNAME")); // 은행명
		model.addAttribute("pay_banknum", request.getParameter("PCD_PAY_BANKNUM")); // 계좌번호
		model.addAttribute("taxsave_rst", request.getParameter("PCD_TAXSAVE_RST")); // 현금영수증 발행결과 (Y|N)

		model.addAttribute("pay_cardname", request.getParameter("PCD_PAY_CARDNAME")); // 카드사명
		model.addAttribute("pay_cardnum", request.getParameter("PCD_PAY_CARDNUM")); // 카드번호
		model.addAttribute("pay_cardtradenum", request.getParameter("PCD_PAY_CARDTRADENUM")); // 카드 거래번호
		model.addAttribute("pay_cardauthno", request.getParameter("PCD_PAY_CARDAUTHNO")); // 카드 승인번호
		model.addAttribute("pay_cardreceipt", request.getParameter("PCD_PAY_CARDRECEIPT")); // 카드 매출전표 URL

		return "order_result";
	}

	/*
	 * payRefund : 결제취소
	 */
	@ResponseBody
	@PostMapping(value = "/payRefund")
	public JSONObject payRefund(HttpServletRequest request) {

		JSONObject jsonObject = new JSONObject();
		JSONParser jsonParser = new JSONParser();

		// 결제취소 전 파트너 인증
		Map<String, String> refundParams = new HashMap<>();
		refundParams.put("PCD_PAYCANCEL_FLAG", "Y");

		JSONObject authObj = new JSONObject();
		authObj = payAuth(refundParams);

		// 파트너 인증 응답값
		String cstId = (String) authObj.get("cst_id"); // 파트너사 ID
		String custKey = (String) authObj.get("custKey"); // 파트너사 키
		String authKey = (String) authObj.get("AuthKey"); // 인증 키
		String payRefURL = (String) authObj.get("return_url"); // 결제취소요청 URL

		// 결제취소 요청 파라미터
		String refund_key = "a41ce010ede9fcbfb3be86b24858806596a9db68b79d138b147c3e563e1829a0"; // (필수) 환불키
		String pay_oid = request.getParameter("PCD_PAY_OID"); // (필수) 주문번호
		String pay_date = request.getParameter("PCD_PAY_DATE"); // (필수) 원거래 결제일자
		String refund_total = request.getParameter("PCD_REFUND_TOTAL"); // (필수) 결제취소 요청금액
		String refund_taxtotal = request.getParameter("PCD_REFUND_TAXTOTAL"); // 결제취소 부가세

		try {

			// 결제취소 요청 전송
			JSONObject refundObj = new JSONObject();

			refundObj.put("PCD_CST_ID", cstId);
			refundObj.put("PCD_CUST_KEY", custKey);
			refundObj.put("PCD_AUTH_KEY", authKey);
			refundObj.put("PCD_REFUND_KEY", refund_key);
			refundObj.put("PCD_PAYCANCEL_FLAG", "Y");
			refundObj.put("PCD_PAY_OID", pay_oid);
			refundObj.put("PCD_PAY_DATE", pay_date);
			refundObj.put("PCD_REFUND_TOTAL", refund_total);
			refundObj.put("PCD_REFUND_TAXTOTAL", refund_taxtotal);

			URL url = new URL(payRefURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			con.setRequestMethod("POST");
			con.setRequestProperty("content-type", "application/json");
			con.setRequestProperty("referer", "http://localhost:8080");
			con.setDoOutput(true);

			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(refundObj.toString());
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
	 * payCertSend : 결제요청 재컨펌 (CERT)
	 */
	@ResponseBody
	@PostMapping(value = "/payCertSend")
	public JSONObject payCertSend(HttpServletRequest request) {

		JSONObject jsonObject = new JSONObject();
		JSONParser jsonParser = new JSONParser();
		
		// 결제요청 재컨펌(CERT) 데이터 - 필수
		String auth_key = request.getParameter("PCD_AUTH_KEY"); // 파트너 인증 토큰 값
		String payer_id = request.getParameter("PCD_PAYER_ID"); // 결제자 고유 ID (빌링키)
		String pay_reqkey = request.getParameter("PCD_PAY_REQKEY"); // 최종 결제요청 승인키
		String pay_cofurl = request.getParameter("PCD_PAY_COFURL"); // 최종 결제요청 URL
		
		try {

			// 결제요청 재컨펌(CERT) 요청 전송
			JSONObject refundObj = new JSONObject();

			refundObj.put("PCD_CST_ID", "test"); //cst_id
			refundObj.put("PCD_CUST_KEY", "abcd1234567890");
			refundObj.put("PCD_AUTH_KEY", auth_key);
			refundObj.put("PCD_PAYER_ID", payer_id);
			refundObj.put("PCD_PAY_REQKEY", pay_reqkey);

			URL url = new URL(pay_cofurl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			con.setRequestMethod("POST");
			con.setRequestProperty("content-type", "application/json");
			con.setRequestProperty("referer", "http://localhost:8080");
			con.setDoOutput(true);

			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(refundObj.toString());
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

		}catch (Exception e) {
			e.printStackTrace();
		}

		return jsonObject;
	}
	

	/* Payple API - Routing */

	// URL링크결제 생성 (linkReg.jsp)
	@RequestMapping(value = "/linkReg")
	public String linkRegRoute() {
		return "linkReg";
	}

	// 결제결과 조회 (payInfo.jsp)
	@RequestMapping(value = "/payInfo")
	public String payInfoRoute() {
		return "payInfo";
	}

	// 정기결제 재결제(빌링키결제) (paySimpleSend.jsp)
	@RequestMapping(value = "/paySimpleSend")
	public String paySimpleSendRoute(Model model) {

		model.addAttribute("payer_id", ""); // 결제자 고유 ID (빌링키)
		model.addAttribute("pay_goods", "휴대폰"); // 상품명
		model.addAttribute("pay_total", "1000"); // 결제요청금액
		model.addAttribute("payer_no", "1234"); // 결제자 고유번호 (파트너사 회원 회원번호)
		model.addAttribute("payer_email", "test@payple.kr"); // 결제자 이메일

		return "paySimpleSend";
	}

	// 현금영수증 발행 (taxSaveReq.jsp)
	@RequestMapping(value = "/taxSaveReq")
	public String taxSaveReqRoute(Model model) {
		return "taxSaveReq";
	}

	/* Payple API - Request(POST) */

	/*
	 * linkReg : URL링크결제 생성
	 */
	@ResponseBody
	@PostMapping(value = "/linkReg")
	public JSONObject linkReg(HttpServletRequest request) {
		JSONObject jsonObject = new JSONObject();
		JSONParser jsonParser = new JSONParser();

		// URL링크 생성 전 파트너 인증
		Map<String, String> linkParams = new HashMap<>();
		linkParams.put("PCD_PAY_WORK", "LINKREG");

		JSONObject authObj = new JSONObject();
		authObj = payAuth(linkParams);

		// 파트너 인증 응답값
		String cstId = (String) authObj.get("cst_id"); // 파트너사 ID
		String custKey = (String) authObj.get("custKey"); // 파트너사 키
		String authKey = (String) authObj.get("AuthKey"); // 인증 키
		String linkRegURL = (String) authObj.get("return_url"); // 링크URL 생성 요청 URL

		// 링크URL 생성 요청 파라미터
		String pay_work = "LINKREG";// (필수) 요청 작업 구분 (URL링크결제 : LINKREG)
		String pay_type = request.getParameter("PCD_PAY_TYPE"); // (필수) 결제수단 (transfer|card)
		String pay_goods = request.getParameter("PCD_PAY_GOODS"); // (필수) 상품명
		String pay_total = request.getParameter("PCD_PAY_TOTAL"); // (필수) 결제요청금액
		String pay_istax = request.getParameter("PCD_PAY_ISTAX"); // 과세여부
		String pay_taxtotal = request.getParameter("PCD_PAY_TAXTOTAL"); // 부가세(복합과세 적용 시)
		String taxsave_flag = request.getParameter("PCD_TAXSAVE_FLAG"); // 현금영수증 발행요청 (Y|N)
		String link_expiredate = request.getParameter("PCD_LINK_EXPIREDATE"); // URL 결제 만료일

		try {
			// 링크URL 생성 요청 전송
			JSONObject linkRegObj = new JSONObject();

			linkRegObj.put("PCD_CST_ID", cstId);
			linkRegObj.put("PCD_CUST_KEY", custKey);
			linkRegObj.put("PCD_AUTH_KEY", authKey);
			linkRegObj.put("PCD_PAY_WORK", pay_work);
			linkRegObj.put("PCD_PAY_TYPE", pay_type);
			linkRegObj.put("PCD_PAY_GOODS", pay_goods);
			linkRegObj.put("PCD_PAY_TOTAL", pay_total);
			linkRegObj.put("PCD_PAY_ISTAX", pay_istax);
			linkRegObj.put("PCD_PAY_TAXTOTAL", pay_taxtotal);
			linkRegObj.put("PCD_TAXSAVE_FLAG", taxsave_flag);
			linkRegObj.put("PCD_LINK_EXPIREDATE", link_expiredate);

			URL url = new URL(linkRegURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			con.setRequestMethod("POST");
			con.setRequestProperty("content-type", "application/json");
			con.setRequestProperty("referer", "http://localhost:8080");
			con.setDoOutput(true);

			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(linkRegObj.toString());
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

		JSONObject authObj = new JSONObject();
		authObj = payAuth(infoParams);

		// 파트너 인증 응답값
		String cstId = (String) authObj.get("cst_id"); // 파트너사 ID
		String custKey = (String) authObj.get("custKey"); // 파트너사 키
		String authKey = (String) authObj.get("AuthKey"); // 인증 키
		String payInfoURL = (String) authObj.get("return_url"); // 결제결과 조회 URL

		// 결제결과 조회 요청 파라미터
		String pay_type = request.getParameter("PCD_PAY_TYPE"); // (필수) 결제수단 (transfer|card)
		String pay_oid = request.getParameter("PCD_PAY_OID"); // (필수) 주문번호
		String pay_date = request.getParameter("PCD_PAY_DATE"); // (필수) 원거래 결제일자

		try {
			// 결제결과 조회 요청 전송
			JSONObject payInfoObj = new JSONObject();

			payInfoObj.put("PCD_CST_ID", cstId);
			payInfoObj.put("PCD_CUST_KEY", custKey);
			payInfoObj.put("PCD_AUTH_KEY", authKey);
			payInfoObj.put("PCD_PAYCHK_FLAG", "Y");
			payInfoObj.put("PCD_PAY_TYPE", pay_type);
			payInfoObj.put("PCD_PAY_OID", pay_oid);
			payInfoObj.put("PCD_PAY_DATE", pay_date);

			URL url = new URL(payInfoURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			con.setRequestMethod("POST");
			con.setRequestProperty("content-type", "application/json");
			con.setRequestProperty("referer", "http://localhost:8080");
			con.setDoOutput(true);

			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(payInfoObj.toString());
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

		/*
		 * // 결제취소 전 파트너 인증 Map<String, String> refundParams = new HashMap<>();
		 * refundParams.put("PCD_PAYCANCEL_FLAG", "Y"); JSONObject authObj = new
		 * JSONObject(); authObj = payAuth(refundParams);
		 */
		// 파트너 인증 응답 값
		String cstId = (String) authObj.get("cst_id"); // 파트너사 ID
		String custKey = (String) authObj.get("custKey"); // 파트너사 키
		String authKey = (String) authObj.get("AuthKey"); // 인증 키
		String taxSaveRegURL = (String) authObj.get("return_url"); // 현금영수 발행/취소 요청 URL
		System.out.println(taxSaveRegURL);
		
		/*
		 * System.out.println(cstId); System.out.println(custKey);
		 * System.out.println(authKey); System.out.println(taxSaveRegURL);
		 */
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
	 * paySimpleSend : 정기결제 재결제(빌링키 결제)
	 */
	@ResponseBody
	@PostMapping(value = "/paySimpleSend")
	public JSONObject paySimpleSend(HttpServletRequest request) {
		JSONObject jsonObject = new JSONObject();
		JSONParser jsonParser = new JSONParser();

		// 정기결제 재결제 전 파트너 인증
		Map<String, String> bilingParams = new HashMap<>();
		bilingParams.put("PCD_PAY_TYPE", request.getParameter("PCD_PAY_TYPE"));
		bilingParams.put("PCD_SIMPLE_FLAG", "Y");

		JSONObject authObj = new JSONObject();
		authObj = payAuth(bilingParams);

		System.out.println(authObj.toString());

		// 파트너 인증 응답값
		String cstId = (String) authObj.get("cst_id"); // 파트너사 ID
		String custKey = (String) authObj.get("custKey"); // 파트너사 키
		String authKey = (String) authObj.get("AuthKey"); // 인증 키
		String bilingURL = (String) authObj.get("return_url"); // 카드 정기결제 재결제 요청 URL

		// 정기결제 재결제 요청 파라미터
		String pay_type = request.getParameter("PCD_PAY_TYPE"); // (필수) 결제수단 (card | transfer)
		String payer_id = request.getParameter("PCD_PAYER_ID"); // (필수) 결제자 고유 ID (빌링키)
		String pay_goods = request.getParameter("PCD_PAY_GOODS"); // (필수) 상품명
		String pay_total = request.getParameter("PCD_PAY_TOTAL"); // (필수) 결제요청금액
		String pay_oid = request.getParameter("PCD_PAY_OID"); // 주문번호
		String payer_no = request.getParameter("PCD_PAYER_NO"); // 결제자 고유번호 (파트너사 회원 회원번호)
		String payer_name = request.getParameter("PCD_PAYER_NAME"); // 결제자 이름
		String payer_hp = request.getParameter("PCD_PAYER_HP"); // 결제자 휴대전화번호
		String payer_email = request.getParameter("PCD_PAYER_EMAIL"); // 결제자 이메일
		String pay_istax = request.getParameter("PCD_PAY_ISTAX"); // 과세여부
		String pay_taxtotal = request.getParameter("PCD_PAY_TAXTOTAL"); // 부가세(복합과세 적용 시)

		try {
			// 정기결제 재결제 요청 전송
			JSONObject bilingObj = new JSONObject();

			bilingObj.put("PCD_CST_ID", cstId);
			bilingObj.put("PCD_CUST_KEY", custKey);
			bilingObj.put("PCD_AUTH_KEY", authKey);
			bilingObj.put("PCD_PAY_TYPE", pay_type);
			bilingObj.put("PCD_PAYER_ID", payer_id);
			bilingObj.put("PCD_PAY_GOODS", pay_goods);
			bilingObj.put("PCD_SIMPLE_FLAG", "Y");
			bilingObj.put("PCD_PAY_TOTAL", pay_total);
			bilingObj.put("PCD_PAY_OID", pay_oid);
			bilingObj.put("PCD_PAYER_NO", payer_no);
			bilingObj.put("PCD_PAYER_NAME", payer_name);
			bilingObj.put("PCD_PAYER_HP", payer_hp);
			bilingObj.put("PCD_PAYER_EMAIL", payer_email);
			bilingObj.put("PCD_PAY_ISTAX", pay_istax);
			bilingObj.put("PCD_PAY_TAXTOTAL", pay_taxtotal);

			System.out.println(bilingObj.toString());

			URL url = new URL(bilingURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			con.setRequestMethod("POST");
			con.setRequestProperty("content-type", "application/json");
			con.setRequestProperty("charset", "UTF-8");
			con.setRequestProperty("referer", "http://localhost:8080");
			con.setDoOutput(true);

			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.write(bilingObj.toString().getBytes());
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
