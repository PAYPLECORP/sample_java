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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class linkController extends PaypleController {

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
		String card_ver = request.getParameter("PCD_CARD_VER"); // 카드 세부 결제방식 (Default: 01+02)
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
			linkRegObj.put("PCD_CARD_VER", card_ver);
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

}
