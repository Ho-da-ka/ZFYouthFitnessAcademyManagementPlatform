package com.shuzi.managementplatform.security;

import com.shuzi.managementplatform.domain.service.UserAccountService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Initializes default auth users and binds existing coach/student records to login accounts.
 */
@Component
public class AuthBootstrapInitializer {

    private final UserAccountService userAccountService;

    public AuthBootstrapInitializer(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        userAccountService.initializeDefaultAccounts();
        userAccountService.syncExistingCoachAndStudentAccounts();
    }
}
