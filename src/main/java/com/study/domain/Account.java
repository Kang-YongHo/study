package com.study.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of="id")
@Builder @AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;

    private boolean emailVerify;

    private String emailCheckToken;

    private LocalDateTime emailCheckDate;

    private LocalDateTime joinDate;

    private String bio;

    private String url;

    private String occupation;

    private String location;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage;

    private boolean studyCreatedByEmail;
    private boolean studyCreatedByWeb;
    private boolean studyResultByEmail;
    private boolean studyResultByWeb;
    private boolean studyUpdatedByEmail;
    private boolean studyUpdatedByWeb;

    public void generateEmailToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailCheckDate = LocalDateTime.now();
    }

    public void completeSignUp(){
        this.emailVerify = true;
        this.joinDate = LocalDateTime.now();
    }

    public boolean isValidToken(String token) {
        return Objects.equals(this.emailCheckToken, token);
    }

    public boolean canSendConfirmEmail() {
        return this.emailCheckDate.isBefore(LocalDateTime.now().minusHours(1));
    }
}
