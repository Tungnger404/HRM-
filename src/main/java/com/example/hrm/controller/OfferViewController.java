package com.example.hrm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/offers")
@RequiredArgsConstructor
public class OfferViewController {

    @GetMapping("/create-form")
    public String createForm(@RequestParam Integer candidateId,
                             Model model) {

        model.addAttribute("candidateId", candidateId);
        return "offer-create";
    }
}