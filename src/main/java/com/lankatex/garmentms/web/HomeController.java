package com.lankatex.garmentms.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "lankaTex";  // Must match the file name exactly: LankaTex.html
    }
}
