package com.study.account;

import com.study.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    private final JavaMailSender javaMailSender;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Account process(SignUpForm signUpForm) {
        Account save = getAccount(signUpForm);
        save.generateEmailToken();
        sendSignUpEmail(save);
        return save;
    }

    public Account getAccount(SignUpForm signUpForm) {
        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(passwordEncoder.encode(signUpForm.getPassword()))
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

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                account.getNickname(),
                account.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(token);

//        여기가 정석적인 방법이라고 함. 추후에 더 공부해보기
//        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
//                account.getNickname(),
//                account.getPassword()
//        );
//        Authentication authenticate = authenticationManager.authenticate(token);
//        SecurityContext context = SecurityContextHolder.getContext();
//        context.setAuthentication(authenticate);
    }
}
