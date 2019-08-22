package com.lms.controllers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.validation.Valid;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.lms.models.LeaveDetails;
import com.lms.models.UserInfo;
import com.lms.repository.UserInfoRepository;
import com.lms.service.EmailSenderService;
import com.lms.service.LeaveManageService;
import com.lms.service.UserInfoService;

@Controller
public class LeaveManageController {

    @Autowired
    private LeaveManageService leaveManageService;

    @Autowired
    private UserInfoService userInfoService;

    
   	@Autowired
   	private UserInfoRepository userInfoRepository;
   	
   	
   	@Autowired
   	private EmailSenderService emailSenderService;
   	@RequestMapping(value = "/user/apply-leave", method = RequestMethod.GET)
    public ModelAndView applyLeave(ModelAndView mav) {

	mav.addObject("leaveDetails", new LeaveDetails());
	mav.setViewName("applyLeave");
	return mav;
    }

    @RequestMapping(value = "/user/apply-leave", method = RequestMethod.POST)
    public ModelAndView submitApplyLeave(ModelAndView mav, @Valid LeaveDetails leaveDetails,
	    BindingResult bindingResult) {

	UserInfo userInfo = userInfoService.getUserInfo();
	if (bindingResult.hasErrors()) {
	    mav.setViewName("applyLeave");
	} else {
	    leaveDetails.setUsername(userInfo.getEmail());
	    leaveDetails.setEmployeeName(userInfo.getFirstName() + " " + userInfo.getLastName());
	    leaveManageService.applyLeave(leaveDetails);
	    //mav.setViewName("redirect:/user/apply-leave");
	    SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo("ajith.kumar232@gmail.com");
		mailMessage.setSubject("Leave Applied");
		mailMessage.setFrom("paypal.tcs@gmail.com");
		mailMessage.setText("Mr/Mrs, "+userInfo.getFirstName()+"has applied for leave from"+leaveDetails.getFromDate()+"to"+leaveDetails.getToDate()+"for"+leaveDetails.getDuration()+"days.Reason:"+leaveDetails.getReason());
		
		emailSenderService.sendEmail(mailMessage);
		
		mav.addObject("emailId", userInfo.getEmail());
		mav.addObject("userInfo", new UserInfo());
		mav.setViewName("successfulRegisteration");
	}
	return mav;
    }
	
    @RequestMapping(value = "/user/get-all-leaves", method = RequestMethod.GET)
    public @ResponseBody String getAllLeaves(@RequestParam(value = "pending", defaultValue = "false") boolean pending,
	    @RequestParam(value = "accepted", defaultValue = "false") boolean accepted,
	    @RequestParam(value = "rejected", defaultValue = "false") boolean rejected) throws Exception {

	Iterator<LeaveDetails> iterator = leaveManageService.getAllLeaves().iterator();
	if (pending || accepted || rejected)
	    iterator = leaveManageService.getAllLeavesOnStatus(pending, accepted, rejected).iterator();
	JSONArray jsonArr = new JSONArray();
	SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DD");
	Calendar calendar = Calendar.getInstance();

	while (iterator.hasNext()) {
	    LeaveDetails leaveDetails = iterator.next();
	    calendar.setTime(leaveDetails.getToDate());
	    calendar.add(Calendar.DATE, 1);

	    JSONObject jsonObj = new JSONObject();
	    jsonObj.put("title", leaveDetails.getEmployeeName());
	    jsonObj.put("start", dateFormat.format(leaveDetails.getFromDate()));
	    jsonObj.put("end", dateFormat.format(calendar.getTime()));
	    if (leaveDetails.isActive())
		jsonObj.put("color", "#0878af");
	    if (!leaveDetails.isActive() && leaveDetails.isAcceptRejectFlag())
		jsonObj.put("color", "green");
	    if (!leaveDetails.isActive() && !leaveDetails.isAcceptRejectFlag())
		jsonObj.put("color", "red");
	    jsonArr.put(jsonObj);
	}

	return jsonArr.toString();
    }
    
    @RequestMapping(value="/user/manage-leaves",method= RequestMethod.GET)
    public ModelAndView manageLeaves(ModelAndView mav) {

	mav.addObject("leavesList", leaveManageService.getAllActiveLeaves());
	mav.setViewName("manageLeaves");
	return mav;
    }

    @RequestMapping(value = "/user/manage-leaves/{action}/{id}", method = RequestMethod.GET)
    public ModelAndView acceptOrRejectLeaves(ModelAndView mav, @PathVariable("action") String action,
	    @PathVariable("id") int id) {
	LeaveDetails leaveDetails = leaveManageService.getLeaveDetailsOnId(id);
	UserInfo userInfo = userInfoService.getUserInfo();
	if (action.equals("accept")) {
	    leaveDetails.setAcceptRejectFlag(true);
	    leaveDetails.setActive(false);
	    mav.setViewName("redirect:/user/manage-leaves");
	    } else if (action.equals("reject")) {
	    leaveDetails.setAcceptRejectFlag(false);
	    leaveDetails.setActive(false);
	    mav.setViewName("redirect:/user/manage-leaves");
	    }
	leaveManageService.updateLeaveDetails(leaveDetails);
	mav.addObject("successMessage", "Updated Successfully!");
	 SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(leaveDetails.getEmail());
		mailMessage.setSubject("LEAVE REQUEST!");
		mailMessage.setFrom("paypal.tcs@gmail.com");
		mailMessage.setText("Hi, "+userInfo.getFirstName()+" .Response to your leave request is successfully done.login to ur account for more details");
		
		emailSenderService.sendEmail(mailMessage);
		
		mav.addObject("emailId", userInfo.getEmail());
		mav.addObject("userInfo", new UserInfo());
		mav.setViewName("successfulRegisteration");
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
    @RequestMapping(value = "/user/my-leaves", method = RequestMethod.GET)
    public ModelAndView showMyLeaves(ModelAndView mav) {

	UserInfo userInfo = userInfoService.getUserInfo();
	List<LeaveDetails> leavesList = leaveManageService.getAllLeavesOfUser(userInfo.getEmail());
	mav.addObject("leavesList", leavesList);
	mav.setViewName("myLeaves");
	return mav;
    }
}
