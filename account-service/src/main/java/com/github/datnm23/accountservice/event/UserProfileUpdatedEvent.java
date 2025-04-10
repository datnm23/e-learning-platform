package com.github.datnm23.accountservice.event;

import com.github.datnm23.accountservice.statics.Gender;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;

@Getter
public class UserProfileUpdatedEvent extends ApplicationEvent {
    private final String avatarUrl;
    private final String bio;
    private final LocalDate dateOfBirth;
    private final Gender gender;
    private final String phone;

    private final String address;

    private final Boolean emailNotifications;
    private final Boolean pushNotifications;

    public UserProfileUpdatedEvent(Object source, String avatarUrl, String bio, LocalDate dateOfBirth, Gender gender,
                                   String phone, String address, Boolean emailNotifications, Boolean pushNotifications) {
        super(source);
        this.avatarUrl = avatarUrl;
        this.bio = bio;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
        this.emailNotifications = emailNotifications;
        this.pushNotifications = pushNotifications;
    }

}
