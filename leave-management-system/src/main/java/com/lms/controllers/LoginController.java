package com.lms.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.lms.models.UserInfo;
import com.lms.repository.UserInfoRepository;
import com.lms.service.EmailSenderService;
import com.lms.service.LeaveManageService;
import com.lms.service.UserInfoService;


@Controller
public class LoginController {

   
	@Autowired
	private UserInfoRepository userInfoRepository;
	
	
	@Autowired
	private EmailSenderService emailSenderService;

	@Autowired
    private UserInfoService userInfoService;

    @Autowired
    LeaveManageService leaveManageService;

    /**
     * This method opens up the login page if user is not authenticated
     * otherwise redirects the user to user home page.
     * 
     * @return
     */
    @RequestMapping(value = { "/", "/login" }, method = RequestMethod.GET)
    public ModelAndView login(ModelAndView mav) {

	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	UserInfo userInfo = userInfoService.findUserByEmail(auth.getName());

	mav.addObject("userInfo", userInfo);
	if (!(auth instanceof AnonymousAuthenticationToken)) {
	    mav.setViewName("home");
	    return mav;
	}
	mav.setViewName("login");
	return mav;
    }

    /**
     * Opens the registration page to register a new user.
     * 
     * @return ModelAndView
     */
    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public ModelAndView registration(ModelAndView mav) {

	UserInfo userInfo = new UserInfo();
	mav.addObject("userInfo", userInfo);
	mav.setViewName("registration");
	return mav;
    }

    /**
     * Gets the form input from registration page and adds the user to the
     * database.
     * 
     * @param user
     * @param bindResult
     * @return ModelAndView
     */
    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public ModelAndView createNewUser(ModelAndView mav, @Valid UserInfo userInfo, BindingResult bindResult) {

	UserInfo userExists = userInfoService.findUserByEmail(userInfo.getEmail());

	if (userExists != null) {
	    bindResult.rejectValue("email", "error.user", "User already exists with Email id");
	}

	if (bindResult.hasErrors()) {
	    mav.setViewName("registration");
	} else 
	{
		userInfoService.saveUser(userInfo);
	userInfoRepository.save(userInfo);
	mav.addObject("successMessage", "User registered successfully! Awaiting for Manager approval!!");
    mav.addObject("userInfo", new UserInfo());
    mav.setViewName("registration");
		/*userInfoService.saveUser(userInfo);
		userInfoRepository.save(userInfo);
	/*SimpleMailMessage mailMessage = new SimpleMailMessage();
	mailMessage.setTo(userInfo.getEmail());
	mailMessage.setSubject("Complete Registration!");
	mailMessage.setFrom("jananik305@gmail.com");
	mailMessage.setText("To confirm your account, please click here : "+userInfo.getFirstName());
	
	emailSenderService.sendEmail(mailMessage);
	
	mav.addObject("emailId", userInfo.getEmail());
	mav.addObject("userInfo", new UserInfo());
	mav.setViewName("successfulRegisteration");*/
}

return mav;
    }
    public UserInfoRepository getUserRepository() {
		return userInfoRepository;
	}

	public void setUserInfoRepository(UserInfoRepository userInfoRepository) {
		this.userInfoRepository = userInfoRepository;
	}

	

	public EmailSenderService getEmailSenderService() {
		return emailSenderService;
	}

	public void setEmailSenderService(EmailSenderService emailSenderService) {
		this.emailSenderService = emailSenderService;
	}
    /**
     * Shows the admin page after user authentication is done.
     * 
     * @param request
     * @return ModelAndView
     * @throws JSONException
     */
    @RequestMapping(value = "/user/home", method = RequestMethod.GET)
    public ModelAndView home(ModelAndView mav, HttpServletRequest request) throws Exception {

	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	UserInfo userInfo = userInfoService.findUserByEmail(auth.getName());
	request.getSession().setAttribute("userInfo", userInfo);

	mav.addObject("userInfo", userInfo);
	mav.setViewName("home");
	return mav;

    }

}
