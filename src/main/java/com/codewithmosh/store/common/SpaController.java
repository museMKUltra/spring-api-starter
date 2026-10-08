package com.codewithmosh.store.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {
    @GetMapping({
            "/",
            "/{path:[^\\.]*}",
            "/{path:[^\\.]*}/{path2:[^\\.]*}",
            "/{path:[^\\.]*}/{path2:[^\\.]*}/{path3:[^\\.]*}",
            "/{path:[^\\.]*}/{path2:[^\\.]*}/{path3:[^\\.]*}/{path4:[^\\.]*}"
    })
    public String redirect() {
        return "forward:/index.html";
    }
}