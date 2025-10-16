# Sample_java

🏠[페이플 홈페이지](https://www.payple.kr/)<br>
페이플 결제 서비스는 간편결제, 정기결제와 같은 <br>
새로운 비즈니스모델과 서비스를 지원하기 위해 다양한 결제방식을 제공합니다.
<br><br>

## Update (2025.10.20)
결제창 호출 요청 전 프로세스인 파트너 인증 방식이 새롭게 변경되어 코드에 반영되었습니다!<br>
이제 클라이언트 단에서 키 값 하나로(clientKey) 더 빠르고 쉬운 파트너 인증을 통한 결제창 호출을 할 수 있습니다.🧑‍💻
<br><br>

## Documentation
📂 **PaypleAuthenticator.java &nbsp;:** &nbsp;파트너 인증<br>

#### 결제연동
>📂 **orderController.java &nbsp;:** &nbsp;결제/취소 컨트롤러<br>
>📂 **order.jsp &nbsp;:** &nbsp;상품 주문<br>
>📂 **order_confirm.jsp &nbsp;:** &nbsp;주문확정 및 결제<br>
>📂 **order_result.jsp &nbsp;:** &nbsp;결제결과<br>
>📂 **paySimpleSend.jsp &nbsp;:** &nbsp;정기결제 재결제(빌링키결제)<br>

#### 기타 API
>📂 **linkController.java / linkReg.jsp  &nbsp;:** &nbsp;URL링크결제<br>
>📂 **payInfoController.java / payInfo.jsp  &nbsp;:** &nbsp;결제결과 조회<br>
>📂 **PayUserController.java / payUser.jsp  &nbsp;:** &nbsp;등록조회/해지<br>
>📂 **taxSaveController.java / taxSaveReq.jsp  &nbsp;:** &nbsp;현금영수증 발행/취소<br>

<br>

🙋‍ [페이플 API](https://developer.payple.kr) 보러가기
