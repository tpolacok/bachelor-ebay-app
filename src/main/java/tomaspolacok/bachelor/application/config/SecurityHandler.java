package tomaspolacok.bachelor.application.config;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import tomaspolacok.bachelor.application.entities.User;
import tomaspolacok.bachelor.application.services.UserService;


@Component
public class SecurityHandler implements AuthenticationSuccessHandler {
	
	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
	
	@Autowired
	private UserService userService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException  {
	    HttpSession session = request.getSession();
	    User user = userService.findUserByEmail(authentication.getName());
	    
		if (user.getUserPreferences().getPermanentLogin()) {
			session.setMaxInactiveInterval(-1);
		} else {
			session.setMaxInactiveInterval(user.getUserPreferences().getSessionTime());
		}
        redirectStrategy.sendRedirect(request, response, request.getServletPath());
	}
	
}