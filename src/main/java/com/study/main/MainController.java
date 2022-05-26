package com.study.main;

import com.study.account.CurrentUser;
import com.study.domain.Account;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String main(@CurrentUser Account account, Model model){
        if(account != null) model.addAttribute(account);

        return "index";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }
}
