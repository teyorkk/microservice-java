package com.moises.AuthService.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.moises.AuthService.dto.AuthRequest;
import com.moises.AuthService.dto.AuthResponse;
import com.moises.AuthService.enums.Role;
import com.moises.AuthService.exceptions.DuplicateResourceException;
import com.moises.AuthService.exceptions.ResourceNotFoundException;
import com.moises.AuthService.model.User;
import com.moises.AuthService.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

        @Mock
        private UserRepository userRepository;

        @Mock
        private PasswordEncoder passwordEncoder;

        @Mock
        private JwtService jwtService;

        @Mock
        private AuthenticationManager authenticationManager;

        @InjectMocks
        private UserService userService;

        @Test
        void register_shouldReturnAuthResponse_whenRequestIsValid() {
                AuthRequest request = AuthRequest.builder()
                                .email("test@mail.com")
                                .password("plain-pass")
                                .build();
                String encodedPassword = "encoded-pass";
                String token = "jwt-token";

                when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
                when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);
                when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(jwtService.generateToken(any(User.class))).thenReturn(token);

                AuthResponse response = userService.register(request);

                assertNotNull(response);
                assertEquals("test@mail.com", response.getEmail());
                assertEquals(token, response.getToken());
                assertEquals(Role.USER.name(), response.getRole());

                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(userRepository).save(userCaptor.capture());

                User savedUser = userCaptor.getValue();
                assertEquals("test@mail.com", savedUser.getEmail());
                assertEquals(encodedPassword, savedUser.getPassword());
                assertEquals(Role.USER, savedUser.getRole());
        }

        @Test
        void register_shouldThrowDuplicateResourceException_whenEmailAlreadyExists() {
                AuthRequest request = AuthRequest.builder()
                                .email("existing@mail.com")
                                .password("password")
                                .build();
                when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

                assertThrows(DuplicateResourceException.class, () -> userService.register(request));

                verify(userRepository, never()).save(any(User.class));
                verify(passwordEncoder, never()).encode(any(String.class));
                verify(jwtService, never()).generateToken(any(User.class));
        }

        @Test
        void login_shouldReturnAuthResponse_whenCredentialsAreValid() {
                AuthRequest request = AuthRequest.builder()
                                .email("user@mail.com")
                                .password("pass123")
                                .build();
                User user = User.builder()
                                .email("user@mail.com")
                                .password("encoded")
                                .role(Role.USER)
                                .build();
                String token = "access-token";

                when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
                when(jwtService.generateToken(user)).thenReturn(token);

                AuthResponse response = userService.login(request);

                assertNotNull(response);
                assertEquals("user@mail.com", response.getEmail());
                assertEquals(token, response.getToken());
                assertEquals(Role.USER.name(), response.getRole());

                verify(authenticationManager).authenticate(
                                new UsernamePasswordAuthenticationToken("user@mail.com", "pass123"));
                verify(userRepository).findByEmail("user@mail.com");
                verify(jwtService).generateToken(user);
        }

        @Test
        void login_shouldThrowResourceNotFoundException_whenUserDoesNotExistAfterAuthentication() {
                AuthRequest request = AuthRequest.builder()
                                .email("missing@mail.com")
                                .password("pass123")
                                .build();
                when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

                assertThrows(ResourceNotFoundException.class, () -> userService.login(request));

                verify(authenticationManager).authenticate(
                                new UsernamePasswordAuthenticationToken("missing@mail.com", "pass123"));
                verify(jwtService, never()).generateToken(any(User.class));
        }

        @Test
        void login_shouldPropagateAuthenticationException_whenCredentialsAreInvalid() {
                AuthRequest request = AuthRequest.builder()
                                .email("user@mail.com")
                                .password("wrong-pass")
                                .build();

                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                                .thenThrow(new BadCredentialsException("Bad credentials"));

                assertThrows(BadCredentialsException.class, () -> userService.login(request));

                verify(userRepository, never()).findByEmail(any(String.class));
                verify(jwtService, never()).generateToken(any(User.class));
        }

        @Test
        void getAllUser_shouldReturnAllUsers() {
                List<User> users = List.of(
                                User.builder().id(1L).email("one@mail.com").password("pass1").role(Role.USER).build(),
                                User.builder().id(2L).email("two@mail.com").password("pass2").role(Role.USER).build());

                when(userRepository.findAll()).thenReturn(users);

                List<User> result = userService.getAllUser();

                assertEquals(2, result.size());
                assertEquals("one@mail.com", result.get(0).getEmail());
                assertEquals("two@mail.com", result.get(1).getEmail());
                verify(userRepository).findAll();
        }

        @Test
        void getAllUser_shouldReturnEmptyList_whenNoUsersExist() {
                when(userRepository.findAll()).thenReturn(List.of());

                List<User> result = userService.getAllUser();

                assertEquals(0, result.size());
                verify(userRepository).findAll();
        }

        @Test
        void getAllUser_shouldReturnSingleUser() {
                List<User> users = List.of(
                                User.builder().id(1L).email("single@mail.com").password("pass1").role(Role.USER)
                                                .build());

                when(userRepository.findAll()).thenReturn(users);

                List<User> result = userService.getAllUser();

                assertEquals(1, result.size());
                assertEquals("single@mail.com", result.get(0).getEmail());
        }

        @Test
        void register_shouldCorrectlyEncodePassword() {
                AuthRequest request = AuthRequest.builder()
                                .email("test@mail.com")
                                .password("plainPassword123")
                                .build();
                String encodedPassword = "encodedPassword123";

                when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
                when(passwordEncoder.encode("plainPassword123")).thenReturn(encodedPassword);
                when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(jwtService.generateToken(any(User.class))).thenReturn("token");

                userService.register(request);

                verify(passwordEncoder).encode("plainPassword123");
                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(userRepository).save(userCaptor.capture());
                assertEquals(encodedPassword, userCaptor.getValue().getPassword());
        }

        @Test
        void register_shouldGenerateValidJwtToken() {
                AuthRequest request = AuthRequest.builder()
                                .email("test@mail.com")
                                .password("password")
                                .build();
                String token = "valid.jwt.token";

                when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
                when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");
                when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(jwtService.generateToken(any(User.class))).thenReturn(token);

                AuthResponse response = userService.register(request);

                assertEquals(token, response.getToken());
                verify(jwtService).generateToken(any(User.class));
        }

        @Test
        void register_shouldSetDefaultRoleToUser() {
                AuthRequest request = AuthRequest.builder()
                                .email("test@mail.com")
                                .password("password")
                                .build();

                when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
                when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");
                when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(jwtService.generateToken(any(User.class))).thenReturn("token");

                AuthResponse response = userService.register(request);

                assertEquals(Role.USER.toString(), response.getRole());
        }

        @Test
        void login_shouldCallAuthenticationManagerWithCorrectCredentials() {
                AuthRequest request = AuthRequest.builder()
                                .email("user@mail.com")
                                .password("correctPassword")
                                .build();
                User user = User.builder().email("user@mail.com").password("encoded").role(Role.USER).build();

                when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
                when(jwtService.generateToken(user)).thenReturn("token");

                userService.login(request);

                verify(authenticationManager).authenticate(
                                new UsernamePasswordAuthenticationToken("user@mail.com", "correctPassword"));
        }

        @Test
        void login_shouldGenerateTokenAfterSuccessfulAuthentication() {
                AuthRequest request = AuthRequest.builder()
                                .email("user@mail.com")
                                .password("password")
                                .build();
                User user = User.builder().email("user@mail.com").password("encoded").role(Role.USER).build();
                String expectedToken = "jwt.token.here";

                when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
                when(jwtService.generateToken(user)).thenReturn(expectedToken);

                AuthResponse response = userService.login(request);

                assertEquals(expectedToken, response.getToken());
                verify(jwtService).generateToken(user);
        }

        @Test
        void login_shouldReturnUserEmailAndRole() {
                AuthRequest request = AuthRequest.builder()
                                .email("admin@mail.com")
                                .password("password")
                                .build();
                User user = User.builder()
                                .email("admin@mail.com")
                                .password("encoded")
                                .role(Role.ADMIN)
                                .build();

                when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
                when(jwtService.generateToken(user)).thenReturn("token");

                AuthResponse response = userService.login(request);

                assertEquals("admin@mail.com", response.getEmail());
                assertEquals(Role.ADMIN.toString(), response.getRole());
        }

        @Test
        void login_shouldNotProceedToTokenGenerationWhenAuthenticationFails() {
                AuthRequest request = AuthRequest.builder()
                                .email("user@mail.com")
                                .password("wrongPassword")
                                .build();

                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                                .thenThrow(new BadCredentialsException("Authentication failed"));

                assertThrows(BadCredentialsException.class, () -> userService.login(request));

                verify(jwtService, never()).generateToken(any(User.class));
                verify(userRepository, never()).findByEmail(any(String.class));
        }

        @Test
        void register_shouldSaveUserWithCorrectEmail() {
                AuthRequest request = AuthRequest.builder()
                                .email("newemail@mail.com")
                                .password("password")
                                .build();

                when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
                when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");
                when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(jwtService.generateToken(any(User.class))).thenReturn("token");

                AuthResponse response = userService.register(request);

                assertEquals("newemail@mail.com", response.getEmail());
        }

        @Test
        void login_shouldReturnCorrectResponseStructure() {
                AuthRequest request = AuthRequest.builder()
                                .email("user@mail.com")
                                .password("password")
                                .build();
                User user = User.builder()
                                .id(1L)
                                .email("user@mail.com")
                                .password("encoded")
                                .role(Role.USER)
                                .build();

                when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
                when(jwtService.generateToken(user)).thenReturn("test.token");

                AuthResponse response = userService.login(request);

                assertNotNull(response.getEmail());
                assertNotNull(response.getToken());
                assertNotNull(response.getRole());
                assertEquals("user@mail.com", response.getEmail());
                assertEquals("test.token", response.getToken());
                assertEquals("USER", response.getRole());
        }

        @Test
        void register_shouldReturnCorrectResponseStructure() {
                AuthRequest request = AuthRequest.builder()
                                .email("newuser@mail.com")
                                .password("password")
                                .build();

                when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
                when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");
                when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(jwtService.generateToken(any(User.class))).thenReturn("new.token");

                AuthResponse response = userService.register(request);

                assertNotNull(response.getEmail());
                assertNotNull(response.getToken());
                assertNotNull(response.getRole());
                assertEquals("newuser@mail.com", response.getEmail());
                assertEquals("new.token", response.getToken());
                assertEquals("USER", response.getRole());
        }
}
