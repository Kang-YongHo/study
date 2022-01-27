package com.study.account;

import com.study.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;

    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model, SignUpForm signUpForm) {
        model.addAttribute(signUpForm);
        model.addAttribute("localDate", LocalDate.now().getYear());
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors) {
        if (errors.hasErrors()) return "account/sign-up";

        Account account = accountService.process(signUpForm);
        accountService.login(account);
        return "redirect:/";
    }

    @GetMapping("/check-email-token")
    public String check(String token, String email, Model model){
        Account byEmail = accountRepository.findByEmail(email);
        String view = "account/checked-email";

        if(byEmail == null) {
            model.addAttribute("error","email");
            return view;
        }

        if(!byEmail.isValidToken(token)){
            model.addAttribute("error", "token");
            return view;
        }

        byEmail.completeSignUp();
        accountService.login(byEmail);
        model.addAttribute("userNum", accountRepository.count());
        model.addAttribute("nickname",byEmail.getNickname());
        return view;
    }

}
