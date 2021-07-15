package tomaspolacok.bachelor.application.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.ModelAndView;

import tomaspolacok.bachelor.application.entities.User;
import tomaspolacok.bachelor.application.helper.Email;
import tomaspolacok.bachelor.application.helper.PasswordReset;
import tomaspolacok.bachelor.application.services.UserService;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    /**
     * Returns login page to user
     * @return
     */
    @GetMapping(value={"/", "/login"})
    public ModelAndView login(){
    	ModelAndView modelAndView = new ModelAndView();
    	if (userService.getLoggedUser() != null ) {
    		modelAndView.setViewName("redirect:/search");
    	} else {
    		modelAndView.setViewName("login");
    	}
        return modelAndView;
    }
    
    /**
     * Handles activation codes input
     * @param activationCode
     * @return
     */
    @GetMapping(value="/activate/{activationCode}")
	public ModelAndView testActivate(@PathVariable String activationCode) {
    	ModelAndView modelAndView = new ModelAndView();
    	if (userService.activate(activationCode)) {
    		modelAndView.setViewName("redirect:/login?activated=true");
    	} else {
    		modelAndView.setViewName("redirect:/login");
    	}
		return modelAndView;
	}


    /**
     * Returns registration page
     * @return
     */
    @GetMapping(value="/registration")
    public ModelAndView registration(){
        ModelAndView modelAndView = new ModelAndView();
        User user = new User();
        modelAndView.addObject("user", user);
        modelAndView.setViewName("registration");
        return modelAndView;
    }

    /**
     * Handles registration verification
     * @param user
     * @param bindingResult
     * @param host
     * @return
     */
    @PostMapping(value = "/registration")
    public ModelAndView createNewUser(@Valid User user, BindingResult bindingResult, @RequestHeader String host) {
        ModelAndView modelAndView = new ModelAndView();
        User userExists = userService.findUserByEmail(user.getEmail());
        if (userExists != null) {
            bindingResult.rejectValue("email", "error.user", "Email already registered.");
        }
        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("registration");
        } else {
            userService.saveUser(user, host);
            modelAndView.addObject("successMessage", "User has been registered successfully, activation link has been sent to registered email address.");
            modelAndView.addObject("user", new User());
            modelAndView.setViewName("registration");
        }
        return modelAndView;
    }
   
    /**
     * Returns page for password reset request
     * @return
     */
    @GetMapping(value="user/password/request")
    public ModelAndView passwordResetRequestGet(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("email", new Email());
        modelAndView.addObject("changeRequest", true);
        modelAndView.setViewName("passwordReset");
        return modelAndView;
    }
    
    /**
     * Handles reset password sending
     * @param email
     * @param bindingResult
     * @param host
     * @return
     */
    @PostMapping(value="user/password/request")
    public ModelAndView passwordResetRequestPost(@ModelAttribute(value="email") Email email, BindingResult bindingResult, @RequestHeader String host){
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.findUserByEmail(email.getEmail());
        if ( user == null) {
        	bindingResult.rejectValue("email", "error.email", "Email not registered");
        }
        if (bindingResult.hasErrors()) {
			modelAndView.addObject("changeRequest", true);
	        modelAndView.setViewName("passwordReset");
		} else {
			userService.sendPasswordChangeRequest(user, host);
			modelAndView.setViewName("redirect:/user/password/request?requestSent=true");
		}
        return modelAndView;
    }
    
    /**
     * Returns page for resetting password
     * @param resetCode
     * @return
     */
    @GetMapping(value="user/password/reset/{resetCode}")
    public ModelAndView passwordResetGet(@PathVariable String resetCode){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("password", new PasswordReset());
        modelAndView.addObject("resetCode", resetCode);
        modelAndView.addObject("changeRequest", false);
        modelAndView.setViewName("passwordReset");
        return modelAndView;
    }
    
    /**
     * Handles resetting password using new password and reset code
     * @param password
     * @param bindingResult
     * @param resetCode
     * @return
     */
    @PostMapping(value="user/password/reset/{resetCode}")
    public ModelAndView passwordResetPost(@Valid @ModelAttribute(value="password") PasswordReset password, BindingResult bindingResult, @PathVariable String resetCode){
    	ModelAndView modelAndView = new ModelAndView();
    	if (password.getPasswordNew().compareTo(password.getPasswordNewSecond()) != 0) {
			bindingResult.rejectValue("passwordNewSecond", "error.password", "New passwords must match.");
		}
    	if (bindingResult.hasErrors()) {
 			modelAndView.addObject("changeRequest", false);
 			modelAndView.addObject("resetCode", resetCode);
 	        modelAndView.setViewName("passwordReset");			
 		} else {
 			User user = userService.findUserByResetCode(resetCode);
 	        if ( user == null) {
 	        	modelAndView.setViewName("redirect:/login?passwordResetFailed=true");
 	        } else {
	 			userService.resetPasswordByRequest(user, password.getPasswordNew());
	 			modelAndView.setViewName("redirect:/login?passwordReset=true");
 	        }
 		}
        return modelAndView;
    }


}