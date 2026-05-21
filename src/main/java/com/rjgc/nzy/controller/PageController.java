package com.rjgc.nzy.controller;

import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PageController {

    @Value("${knowledge.categories}")
    private List<String> categories;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping({"/", "/index"})
    public String index(Model model) {
        model.addAttribute("categoriesJson", JSON.toJSONString(categories));
        return "index";
    }

    @GetMapping("/knowledge-add")
    public String knowledgeAdd(Model model) {
        model.addAttribute("categoriesJson", JSON.toJSONString(categories));
        return "knowledge-add";
    }

    @GetMapping("/knowledge-list")
    public String knowledgeList(Model model) {
        model.addAttribute("categoriesJson", JSON.toJSONString(categories));
        return "knowledge-list";
    }

    @GetMapping("/ai-qa")
    public String aiQa() {
        return "ai-qa";
    }

    @GetMapping("/stats")
    public String stats() {
        return "stats";
    }
}
