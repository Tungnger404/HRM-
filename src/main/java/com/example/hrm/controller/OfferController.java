package com.example.hrm.controller;

import com.example.hrm.entity.Offer;
import com.example.hrm.service.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/offer")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;

    @PostMapping("/create")
    public String createOffer(@RequestParam Integer candidateId,
                              @RequestParam Double salary,
                              @RequestParam String startDate,
                              @RequestParam String probation) {

        offerService.createOffer(
                candidateId,
                salary,
                LocalDate.parse(startDate),
                probation
        );

        return "redirect:/offer/list";
    }

    @GetMapping("/offer/detail/{id}")
    public String showDetail(@PathVariable Integer id, Model model) {

        Offer offer = offerService.findById(id);
        model.addAttribute("offer", offer);

        return "offer/detail";
    }
    @PostMapping("/update")
    public String updateOffer(@ModelAttribute Offer formOffer) {

        Offer dbOffer = offerService.findById(formOffer.getOfferId());

        dbOffer.setSalaryOffered(formOffer.getSalaryOffered());
        dbOffer.setStartDate(formOffer.getStartDate());
        dbOffer.setProbationPeriod(formOffer.getProbationPeriod());

        offerService.save(dbOffer);

        return "redirect:/offer/manage";
    }

}