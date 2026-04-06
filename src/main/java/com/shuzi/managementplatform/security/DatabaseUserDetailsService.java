package com.shuzi.managementplatform.security;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shuzi.managementplatform.domain.entity.UserAccount;
import com.shuzi.managementplatform.domain.mapper.UserAccountMapper;
import com.shuzi.managementplatform.domain.service.UserAccountService;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Loads login users from database table {@code user_accounts}.
 */
@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserAccountMapper userAccountMapper;

    public DatabaseUserDetailsService(UserAccountMapper userAccountMapper) {
        this.userAccountMapper = userAccountMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalized = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        UserAccount account = userAccountMapper.selectOne(
                Wrappers.<UserAccount>lambdaQuery().eq(UserAccount::getUsername, normalized)
        );
        if (account == null) {
            throw new UsernameNotFoundException("user not found");
        }
        if (!UserAccountService.STATUS_ACTIVE.equals(account.getStatus())) {
            throw new DisabledException("user disabled");
        }

        return User.withUsername(account.getUsername())
                .password(account.getPasswordHash())
                .roles(account.getRole())
                .build();
    }
}
