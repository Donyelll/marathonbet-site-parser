package com.github.marathonbetsiteparser.controllers;

import com.github.marathonbetsiteparser.services.ParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/home")
public class MainController {

    private ParserService parserService;

    @Autowired
    public MainController(ParserService parserService) {
        this.parserService = parserService;
    }

    @GetMapping
    public String home(Model model){

        model.addAttribute("matches", parserService.getAllMatches());
        return "home";
    }



}
