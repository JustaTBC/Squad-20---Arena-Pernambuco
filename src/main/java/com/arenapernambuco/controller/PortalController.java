package com.arenapernambuco.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PortalController {

    @GetMapping("/")
    public String portal() {
        return "portal";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/erro-403")
    public String erro403() {
        return "erro-403";
    }
}
