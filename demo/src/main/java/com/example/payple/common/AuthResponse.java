package com.example.payple.common;

import org.json.simple.JSONObject;

/**
 * Payple 파트너 인증 응답 데이터 클래스
 */
public class AuthResponse {
	private String cstId;
	private String custKey;
	private String authKey;
	private String returnUrl;

	/**
	 * JSONObject로부터 AuthResponse 객체 생성
	 *
	 * @param authObj 인증 응답 JSONObject
	 * @return AuthResponse 객체
	 */
	public static AuthResponse from(JSONObject authObj) {
		AuthResponse response = new AuthResponse();
		response.cstId = (String) authObj.get("cst_id");
		response.custKey = (String) authObj.get("custKey");
		response.authKey = (String) authObj.get("AuthKey");
		response.returnUrl = (String) authObj.get("return_url");
		return response;
	}

	public String getCstId() {
		return cstId;
	}

	public String getCustKey() {
		return custKey;
	}

	public String getAuthKey() {
		return authKey;
	}

	public String getReturnUrl() {
		return returnUrl;
	}
}