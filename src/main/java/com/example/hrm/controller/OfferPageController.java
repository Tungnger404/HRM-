package com.example.hrm.controller;

import com.example.hrm.entity.*;
import com.example.hrm.repository.CandidateRepository;
import com.example.hrm.repository.InterviewRepository;
import com.example.hrm.repository.OfferRepository;
import com.example.hrm.service.EmployeeService;
import com.example.hrm.service.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/offer")
@RequiredArgsConstructor
public class OfferPageController {

    private final CandidateRepository candidateRepository;
    private final OfferRepository offerRepository;
    private final OfferService offerService;
    private final InterviewRepository interviewRepository;
    private final EmployeeService employeeService;

    @GetMapping("/list")
    public String pipeline(Model model) {
        List<Interview> passedRound2 = interviewRepository.findByRoundNumberAndResult(2, InterviewResult.PASS);

        List<Interview> eligible = passedRound2.stream()
                .filter(i -> !offerRepository.existsByCandidate(i.getCandidate()))
                .toList();

        model.addAttribute("interviews", eligible);
        return "offer/list";
    }

    @GetMapping("/manage")
    public String manageOffers(Model model, @RequestParam(required = false) String keyword) {
        List<Offer> offers = (keyword != null && !keyword.isBlank())
                ? offerRepository.findByCandidate_FullNameContainingIgnoreCase(keyword)
                : offerRepository.findAll();

        model.addAttribute("offers", offers);
        model.addAttribute("keyword", keyword);
        return "offer/manage";
    }

    @GetMapping("/create/{candidateId}")
    public String showCreateOffer(@PathVariable Integer candidateId, Model model) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));
        model.addAttribute("candidate", candidate);
        return "offer/create";
    }

    @PostMapping("/save")
    public String saveOffer(@RequestParam Integer candidateId,
                            @RequestParam Double salary,
                            @RequestParam String startDate,
                            @RequestParam String probation) {
        offerService.createOffer(candidateId, salary, LocalDate.parse(startDate), probation);
        return "redirect:/offer/manage";
    }

    @GetMapping("/detail/{offerId}")
    public String offerDetail(@PathVariable Integer offerId, Model model) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));
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

    @PostMapping("/send/{offerId}")
    public String sendOffer(@PathVariable Integer offerId) {
        offerService.sendOffer(offerId);
        return "redirect:/offer/detail/" + offerId;
    }

    @GetMapping("/accept/{offerId}")
    public String acceptOffer(@PathVariable Integer offerId) {
        offerService.acceptOffer(offerId);
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));
        return "redirect:/offer/onboarding/" + offer.getCandidate().getCandidateId();
    }

    @GetMapping("/reject/{offerId}")
    public String rejectOffer(@PathVariable Integer offerId) {
        offerService.rejectOffer(offerId);
        return "offer/reject-success";
    }


    @GetMapping("/onboarding/{candidateId}")
    public String showOnboarding(@PathVariable Integer candidateId, Model model) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));
        if (candidate.getStatus() == CandidateStatus.HIRED) {
            return "redirect:/offer/onboarding-success";
        }
        if (candidate.getStatus() == CandidateStatus.OFFER_ACCEPTED) {
            candidate.setStatus(CandidateStatus.ONBOARDING);
            candidateRepository.save(candidate);
        }
        model.addAttribute("candidate", candidate);
        return "onboarding/form";
    }

    @PostMapping("/onboarding/complete")
    public String completeOnboarding(@RequestParam Integer candidateId,
                                     @RequestParam String identityCard,
                                     @RequestParam String taxCode,
                                     @RequestParam(required = false) String joinDateStr) {
        LocalDate joinDate = (joinDateStr != null && !joinDateStr.isEmpty())
                ? LocalDate.parse(joinDateStr) : LocalDate.now();
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));
        RecruitmentRequest req = candidate.getJobPosting().getRecruitmentRequest();
        employeeService.createEmployeeFromCandidate(
                candidateId,
                joinDate,
                req.getJobPosition().getJobId(),
                req.getDepartment().getDeptId(),
                identityCard,
                taxCode
        );

        return "redirect:/offer/onboarding-success";
    }
    @GetMapping("/onboarding-success")
    public String showSuccessPage() {
        return "onboarding/success";
    }
}