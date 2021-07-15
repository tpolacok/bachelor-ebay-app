package tomaspolacok.bachelor.application.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HelpController {

	/**
	 * Servlets which displays help page
	 * @return
	 */
	@RequestMapping(value={"/help"}, method = RequestMethod.GET)
	public ModelAndView help(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("help");
        return modelAndView;
    }
}
