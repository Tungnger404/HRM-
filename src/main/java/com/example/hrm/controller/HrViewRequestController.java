//package com.example.hrm.controller;
//
//import com.example.hrm.service.RequestService;
//import org.apache.catalina.connector.Request;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//
//import java.util.List;
//
//@Controller
//@RequestMapping ("/hr")
//public class HrViewRequestController {
//    @Autowired
//    public RequestService requestService;
//    @GetMapping ("/view-request")
//    public String getRequests(Model model){
//
//        //List<Request> requests = requestService.getAll();
//
//        //model.addAttribute("requests", requests);
//
//        return "hr/view_request";
//    }
//
//}
