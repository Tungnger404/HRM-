package com.example.hrm.controller;

import com.example.hrm.repository.BankAccountRepository;
import com.example.hrm.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/bank/portal")
public class BankPortalController {

    private final PayrollService payrollService;
    private final BankAccountRepository bankAccountRepo;

    @GetMapping
    public String portal(@RequestParam(value = "batchIds", required = false) String batchIds,
                         @RequestParam(value = "auto", required = false) Integer auto,
                         Model model) {
        model.addAttribute("batchIds", batchIds);
        model.addAttribute("auto", auto != null && auto == 1);
        return "bank/portal";
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@RequestParam("batchIds") String batchIds) {
        List<Integer> ids = Arrays.stream(batchIds.split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .map(Integer::parseInt).toList();

        byte[] xlsx = payrollService.exportBankTransferExcel(ids);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=salary-transfer-" + System.currentTimeMillis() + ".xlsx")
                .body(xlsx);
    }

    @GetMapping("/accounts")
    public String accounts(Model model) {
        model.addAttribute("accounts", bankAccountRepo.findAll());
        return "bank/portal-accounts";
    }
}
