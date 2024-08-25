package testcases;

import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import pojo.CreateOrderRequest;
import pojo.LoginRequest;
import pojo.LoginResponse;
import pojo.OrderDetails;

import static io.restassured.RestAssured.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class EcommAPITest {
	@Test
	public void ecommTest() {
		
		//login 
		RequestSpecification req= new RequestSpecBuilder().setContentType(ContentType.JSON).setBaseUri("https://rahulshettyacademy.com").build();
		
		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setUserEmail("sachin132@gmail.com");
		loginRequest.setUserPassword("Titanic123@");
		RequestSpecification reqSpec=  given().relaxedHTTPSValidation().log().all().spec(req).body(loginRequest);
		
		ResponseSpecification res = new ResponseSpecBuilder().expectContentType(ContentType.JSON).expectStatusCode(200).build();
	
		
		LoginResponse loginResponse =reqSpec.when().post("/api/ecom/auth/login").
		then().spec(res).extract().as(LoginResponse.class);
		
		String token = loginResponse.getToken();
		String userId= loginResponse.getUserId();
		
		//add product
		RequestSpecification reqProductBaseReq = new RequestSpecBuilder().addHeader("authorization", token).setBaseUri("https://rahulshettyacademy.com").build();
		
		RequestSpecification reqAddProduct=  given().spec(reqProductBaseReq).
		param("productName", "Polo").
		param("productAddedBy", userId).
		param("productCategory","fashion").
		param("productSubCategory","Shirts").
		param("productPrice","500").
		param("productDescription","US Polo").
		param("productFor","Men").
		multiPart("productImage", new File("/Users/himaniwadhawan/Desktop/USPolo.jpg"));
		
		String response= reqAddProduct.when().post("api/ecom/product/add-product").
		then().log().all().assertThat().extract().asString();
		
		JsonPath js = new JsonPath(response);
		String productId  = js.get("productId");
		String message = js.get("message");
		System.out.println(message);
		System.out.println(productId);
		
		// Create an order
		
		// Set up the base request specification
		RequestSpecification reqCreateOrderBaseReq = new RequestSpecBuilder()
		    .setBaseUri("https://rahulshettyacademy.com")
		    .addHeader("authorization", token)
		    .setContentType(ContentType.JSON)
		    .build();

		// Create order request details
		OrderDetails ordDetails = new OrderDetails();
		ordDetails.setCountry("India");
		ordDetails.setProductOrderedId(productId);

		// Add order details to the list
		ArrayList<OrderDetails> ordDetailsList = new ArrayList<OrderDetails>();
		ordDetailsList.add(ordDetails);
		CreateOrderRequest createOrdDetails = new CreateOrderRequest();
		createOrdDetails.setOrders(ordDetailsList);

		// Prepare the request with specifications and body
		RequestSpecification reqCreateOrd = given()
		    .log().all()
		    .spec(reqCreateOrderBaseReq)
		    .body("{\n"
		    		+ "    \"orders\": [\n"
		    		+ "        {\n"
		    		+ "            \"country\": \"India\",\n"
		    		+ "            \"productOrderedId\": \""+productId+"\"\n"
		    		+ "        }\n"
		    		+ "    ]\n"
		    		+ "}\n"
		    		+ "");

		// Send the POST request and capture the response
		String responseCreateOrder = reqCreateOrd.when()
		    .post("/api/ecom/order/create-order")
		    .then()
		    .log().all()
		    .extract()
		    .response()
		    .asString();

		// Extract the message from the response
		
		JsonPath js2 = new JsonPath(responseCreateOrder);
		String message2 = js2.get("message");
		String orderId = js2.get("orders[0]");

		System.out.println("Response Message: " + message2);
		System.out.println(orderId);
		
		//view order details
		RequestSpecification getOrderDeatils = given().log().all().spec(reqProductBaseReq).queryParam("id",orderId );
		
		String responseGetOrder= getOrderDeatils.when().get("api/ecom/order/get-orders-details").
								 then().log().all().extract().asString();	
		
		System.out.println(responseGetOrder);
		
		//Delete the product
		
		RequestSpecification deleteProduct = given().log().all().spec(reqProductBaseReq).pathParam("productId", productId);
		String responseDeleteProd = deleteProduct.delete("/api/ecom/product/delete-product/{productId}").then().
									log().all().extract().asString();
		System.out.println(responseDeleteProd);
	
	
	}
	
	

}
