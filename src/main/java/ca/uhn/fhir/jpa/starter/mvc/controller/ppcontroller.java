package ca.uhn.fhir.jpa.starter.mvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ppcontroller {

	@GetMapping("/")
	public String x() {
		return "about";
	}
}
