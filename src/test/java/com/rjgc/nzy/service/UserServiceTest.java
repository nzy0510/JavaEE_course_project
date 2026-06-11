package com.rjgc.nzy.service;

import com.rjgc.nzy.entity.User;
import com.rjgc.nzy.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private final UserMapper userMapper = mock(UserMapper.class);
    private final UserService userService = new UserService(userMapper);

    @Test
    void registerCreatesUserWithDefaultUserRole() {
        when(userMapper.selectCount(any())).thenReturn(0L);

        userService.register("new-user", "secret");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(userCaptor.capture());
        assertThat(userCaptor.getValue().getRole()).isEqualTo("USER");
    }

    @Test
    void registerCreatesConfiguredAdminWithAdminRole() {
        when(userMapper.selectCount(any())).thenReturn(0L);

        userService.register("nzy333", "secret");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(userCaptor.capture());
        assertThat(userCaptor.getValue().getRole()).isEqualTo("ADMIN");
    }
}
