package net.fluance.security.auth.web.controller.access;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class LoginContollerTest {
	private final static String EXPECTED_PARAMETER = "token=foo";
	private final static String TOKEN = "foo";
	
	@Test
	public void compundRedirectUrl_should_return_with_and() {
		LoginContoller logingController = new LoginContoller();
		String url = "https://mojito.dev.fluance.net/#/whiteboard-surgery?date=Fri%20Jan%2031%202020%2015:10:36%20GMT%2B0100";
		
		String expectedResult = url.concat("&").concat(EXPECTED_PARAMETER);
		
		assertEquals("Must match", logingController.compundRedirectUrl(url, TOKEN), expectedResult);
	}
	
	@Test
	public void compundRedirectUrl_should_return_with_question_mark() {
		LoginContoller logingController = new LoginContoller();
		String url = "https://mojito.dev.fluance.net/#/whiteboard-surgery";
		
		String expectedResult = url.concat("?").concat(EXPECTED_PARAMETER);
		
		assertEquals("Must match", logingController.compundRedirectUrl(url, TOKEN), expectedResult);
	}
}
