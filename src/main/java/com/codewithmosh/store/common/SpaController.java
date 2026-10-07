package com.codewithmosh.store.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {
    @RequestMapping(value = "{path:[^\\.]*}")
    public String redirect() {
        // Forward to home page so React Router can take over
        return "forward:/index.html";
    }
}