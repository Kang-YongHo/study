package com.study.account;

import com.study.LocalMailSender;
import com.study.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    private final JavaMailSender javaMailSender;

    public void process(SignUpForm signUpForm) {
        Account save = getAccount(signUpForm);
        save.generateEmailToken();
        sendSignUpEmail(save);
    }

    public Account getAccount(SignUpForm signUpForm) {
        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(signUpForm.getPassword())
                .studyCreatedByWeb(true)
                .studyResultByWeb(true)
                .studyUpdatedByWeb(true)
                .build();

        Account save = accountRepository.save(account);
        return save;
    }

    public void sendSignUpEmail(Account save) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(save.getEmail());
        msg.setSubject("가입 인증 ㄱ ㄱ");
        msg.setText("/check-email-token?token=" + save.getEmailCheckToken() + "&email=" + save.getEmail());
        javaMailSender.send(msg);
    }

}
