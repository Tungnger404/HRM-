package com.example.hrm.controller;

import com.example.hrm.entity.*;
import com.example.hrm.repository.CandidateRepository;
import com.example.hrm.repository.InterviewRepository;
import com.example.hrm.repository.OfferRepository;
import com.example.hrm.service.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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

    // =====================================================
    // 1️⃣ CANDIDATE PIPELINE
    // =====================================================
    @GetMapping("/list")
    public String pipeline(Model model) {

        // Lấy các interview round 2 PASS
        List<Interview> passedRound2 =
                interviewRepository.findByRoundNumberAndResult(
                        2,
                        InterviewResult.PASS
                );

        // Lọc những người chưa có Offer
        List<Interview> eligible = passedRound2.stream()
                .filter(i -> !offerRepository.existsByCandidate(i.getCandidate()))
                .toList();

        model.addAttribute("interviews", eligible);

        return "offer/list";
    }

    // =====================================================
    // 2️⃣ OFFER MANAGEMENT (có search)
    // =====================================================
    @GetMapping("/manage")
    public String manageOffers(Model model,
                               @RequestParam(required = false) String keyword) {

        List<Offer> offers;

        if (keyword != null && !keyword.isBlank()) {
            offers = offerRepository
                    .findByCandidate_FullNameContainingIgnoreCase(keyword);
        } else {
            offers = offerRepository.findAll();
        }

        model.addAttribute("offers", offers);
        model.addAttribute("keyword", keyword);

        return "offer/manage";
    }

    // =====================================================
    // 3️⃣ SHOW CREATE OFFER FORM
    // =====================================================
    @GetMapping("/create/{candidateId}")
    public String showCreateOffer(@PathVariable Integer candidateId,
                                  Model model) {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        model.addAttribute("candidate", candidate);

        return "offer/create";
    }

    // =====================================================
    // 4️⃣ SAVE OFFER
    // =====================================================
    @PostMapping("/save")
    public String saveOffer(@RequestParam Integer candidateId,
                            @RequestParam Double salary,
                            @RequestParam String startDate,
                            @RequestParam String probation) {

        offerService.createOffer(
                candidateId,
                salary,
                LocalDate.parse(startDate),
                probation
        );

        // Sau khi tạo → quay về manage
        return "redirect:/offer/manage";
    }

    // =====================================================
    // 5️⃣ OFFER DETAIL
    // =====================================================
    @GetMapping("/detail/{offerId}")
    public String offerDetail(@PathVariable Integer offerId,
                              Model model) {

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));

        model.addAttribute("offer", offer);

        return "offer/detail";
    }

    // =====================================================
    // 6️⃣ SEND OFFER
    // =====================================================
    @PostMapping("/send/{offerId}")
    public String sendOffer(@PathVariable Integer offerId) {

        offerService.sendOffer(offerId);

        return "redirect:/offer/detail/" + offerId;
    }

    // =====================================================
    // 7️⃣ ACCEPT OFFER
    // =====================================================
    @GetMapping("/accept/{offerId}")
    public String acceptOffer(@PathVariable Integer offerId) {

        offerService.acceptOffer(offerId);

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));

        Integer candidateId = offer.getCandidate().getCandidateId();

        // 🔥 Redirect sang onboarding
        return "redirect:/offer/onboarding/" + candidateId;
    }
    // =====================================================
    // 8️⃣ REJECT OFFER
    // =====================================================
    @GetMapping("/reject/{offerId}")
    public String rejectOffer(@PathVariable Integer offerId) {

        offerService.rejectOffer(offerId);

        return "offer/reject-success";
    }
    @GetMapping("/onboarding/{candidateId}")
    public String showOnboarding(@PathVariable Integer candidateId,
                                 Model model) {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        if (candidate.getStatus() != CandidateStatus.OFFER_ACCEPTED) {
            throw new RuntimeException("Candidate not ready for onboarding");
        }

        candidate.setStatus(CandidateStatus.ONBOARDING);
        candidateRepository.save(candidate);

        model.addAttribute("candidate", candidate);

        return "onboarding/form";
    }


}