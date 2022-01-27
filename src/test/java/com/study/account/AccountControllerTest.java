package com.study.account;

import com.study.domain.Account;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    JavaMailSender javaMailSender;

    @Test
    void 인증메일_가입_입력값_오류() throws Exception {
        mockMvc.perform(get("/check-email-token")
                        .param("token", "sadfsadfsafsdfa")
                        .param("email", "test@mail.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(unauthenticated());
    }

    @Test
    void 인증메일_가입_입력값_정상() throws Exception {
        Account test = Account.builder()
                .email("test@amil.com")
                .nickname("test")
                .password("12345678")
                .build();
        Account save = accountRepository.save(test);
        save.generateEmailToken();

        mockMvc.perform(get("/check-email-token")
                        .param("token", save.getEmailCheckToken())
                        .param("email", save.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(model().attributeExists("userNum"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(authenticated());
    }

    @Test
    void 회원가입_폼_테스트() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"))
                .andExpect(unauthenticated());
    }

    @Test
    void 회원가입_처리_잘못된_입력값() throws Exception {
        mockMvc.perform(post("/sign-up")
                        .param("nickname", "test")
                        .param("email", "mailaaa")
                        .param("password", "asdf")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(unauthenticated());
    }

    @Test
    void 회원가입_처리_정상_입력값() throws Exception {
        mockMvc.perform(post("/sign-up")
                        .param("nickname", "test")
                        .param("email", "mail@mail.com")
                        .param("password", "qwerasdf")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(authenticated().withUsername("test"));

        Account byEmail = accountRepository.findByEmail("mail@mail.com");

        assertNotNull(byEmail);
        assertNotEquals(byEmail.getPassword(), "qwerasdf");
        assertTrue(accountRepository.existsByEmail("mail@mail.com"));
        assertNotNull(byEmail.getEmailCheckToken());
        then(javaMailSender).should().send(any(SimpleMailMessage.class));
    }
}