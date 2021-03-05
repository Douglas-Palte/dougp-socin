package br.com.dougp.socin.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WebController implements ErrorController {
	@GetMapping({ "/" })
	public String index(Model model) {
		return "index";
	}

	@RequestMapping("/error")
	@ResponseBody
	public String handleError(HttpServletRequest request) {
		Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
		Exception exception = (Exception) request.getAttribute("org.springframework.web.servlet.DispatcherServlet.EXCEPTION");
		return String.format("<html><body><h2>Error Page</h2><div>Status code: <b>%s</b></div>" + "<div>Exception Message: <b>%s</b></div><body></html>", statusCode,
				exception == null ? "N/A" : exception.getMessage());
	}

	@Override
	public String getErrorPath() {
		return "/error";
	}
}
