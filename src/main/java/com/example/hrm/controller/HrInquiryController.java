package com.example.hrm.controller;

import com.example.hrm.service.PayrollInquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/hr/inquiries")
public class HrInquiryController {

    private final PayrollInquiryService payrollInquiryService;

    /**
     * Hiển thị danh sách các inquiry cho HR.
     *
     * @param status trạng thái lọc (có thể null hoặc rỗng để lấy tất cả)
     * @param model  đối tượng Model để truyền dữ liệu sang view
     * @return tên view hiển thị danh sách inquiry (hr/inquiry-list)
     */
    @GetMapping
    public String list(@RequestParam(value = "status", required = false) String status,
                       Model model) {
        model.addAttribute("status", status == null ? "" : status);
        model.addAttribute("inquiries", payrollInquiryService.listInquiriesForHr(status));
        return "hr/inquiry-list";
    }

    /**
     * Hiển thị chi tiết một inquiry cụ thể.
     *
     * @param id    ID của inquiry
     * @param model đối tượng Model để truyền dữ liệu sang view
     * @return tên view hiển thị chi tiết inquiry (hr/inquiry-detail)
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("inq", payrollInquiryService.getInquiry(id));
        return "hr/inquiry-detail";
    }

    /**
     * Xử lý phản hồi (resolve) inquiry từ phía HR.
     * <p>
     * Nếu thành công: hiển thị thông báo success.
     * Nếu lỗi: hiển thị thông báo lỗi.
     * </p>
     *
     * @param id     ID của inquiry cần xử lý
     * @param answer nội dung phản hồi của HR
     * @param ra     RedirectAttributes để truyền thông báo qua redirect
     * @return redirect về trang chi tiết inquiry
     */
    @PostMapping("/{id}/resolve")
    public String resolve(@PathVariable Integer id,
                          @RequestParam("answer") String answer,
                          RedirectAttributes ra) {
        try {
            payrollInquiryService.resolveInquiryByHr(id, answer);
            ra.addFlashAttribute("msgType", "success");
            ra.addFlashAttribute("msg", "Đã phản hồi inquiry thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("msgType", "danger");
            ra.addFlashAttribute("msg", e.getMessage());
        }
        return "redirect:/hr/inquiries/" + id;
    }
}