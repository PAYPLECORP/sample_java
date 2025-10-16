package com.example.payple.common;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.simple.JSONObject;

public class HttpUtil {

	private HttpUtil() {
	}

	/**
	 * Payple API용 HttpURLConnection 생성
	 *
	 * @param urlString 요청 URL
	 * @return 설정된 HttpURLConnection 객체
	 * @throws Exception URL 파싱 또는 연결 오류 발생 시
	 */
	public static HttpURLConnection createPaypleConnection(String urlString) throws Exception {
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();

		con.setRequestMethod("POST");
		con.setRequestProperty("content-type", "application/json");
		con.setRequestProperty("referer", "http://localhost:8080");
		con.setDoOutput(true);

		return con;
	}

	/**
	 * JSON 요청 전송
	 *
	 * @param con HttpURLConnection 객체
	 * @param requestObj 전송할 JSONObject
	 * @throws Exception IO 오류 발생 시
	 */
	public static void sendJsonRequest(HttpURLConnection con, JSONObject requestObj) throws Exception {
		try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
			wr.write(requestObj.toString().getBytes("UTF-8"));
			wr.flush();
		}
	}

	/**
	 * Payple API 기본 요청 객체 생성 (공통 파라미터 포함)
	 *
	 * @param cstId 파트너사 ID
	 * @param custKey 파트너사 키
	 * @param authKey 인증 키
	 * @return 기본 파라미터가 설정된 JSONObject
	 */
	public static JSONObject createBasePaypleRequest(String cstId, String custKey, String authKey) {
		JSONObject obj = new JSONObject();
		obj.put("PCD_CST_ID", cstId);
		obj.put("PCD_CUST_KEY", custKey);
		obj.put("PCD_AUTH_KEY", authKey);
		return obj;
	}

	/**
	 * HTTP 응답을 읽어서 문자열로 반환
	 *
	 * @param con HttpURLConnection 객체
	 * @return 응답 본문 문자열
	 * @throws Exception HTTP 오류 또는 IO 오류 발생 시
	 */
	public static String readResponse(HttpURLConnection con) throws Exception {
		// HTTP 응답 상태 코드 확인
		int responseCode = con.getResponseCode();
		if (responseCode != 200) {
			throw new RuntimeException("HTTP Error Code: " + responseCode);
		}

		// try-with-resources로 자동 close
		try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
			StringBuilder response = new StringBuilder();
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}

			return response.toString();
		}
	}
}