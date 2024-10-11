package com.tkk.controller;

import com.tkk.config.JwtProvider;
import com.tkk.modal.User;
import com.tkk.repository.UserRepository;
import com.tkk.request.LoginRequest;
import com.tkk.response.AuthResponse;
import com.tkk.service.CustomUserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomUserDetailsImpl customUserDetails;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signupHandler(@RequestBody User user) throws Exception {
        // check if user with the same email already exists
        User isUserExist = userRepository.findByEmail(user.getEmail());

        // if user exists, throw an exception
        if (isUserExist != null) {
            throw new Exception("Email has already exist with another account");
        }

        // create a new user object
        User createdUser = new User();

        // set the encrypted password, email, and full name for the new user
        createdUser.setPassword(passwordEncoder.encode(user.getPassword()));
        createdUser.setEmail(user.getEmail());
        createdUser.setFullName(user.getFullName());

        // save the new user in the database
        User savedUser = userRepository.save(createdUser);

        // Authenticate the user with email and password
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // generate JWT token for the authenticated user
        String jwt = JwtProvider.generateToken(authentication);

        // create a response object with success message and token
        AuthResponse res = new AuthResponse();
        res.setMessage("Signup success");
        res.setJwt(jwt);

        // return the saved user with HTTP status 201 crated
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginHandler(@RequestBody LoginRequest loginRequest) {
        String username = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        Authentication authentication = authenticate(username, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = JwtProvider.generateToken(authentication);

        AuthResponse res = new AuthResponse();
        res.setMessage("Login Success");
        res.setJwt(jwt);

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    private Authentication authenticate(String username, String password) {
        // load the user details based on the provided username
        UserDetails userDetails = customUserDetails.loadUserByUsername(username);
        // if the user does not exist, throw an exception for invalid username
        if (userDetails == null) {
            throw new BadCredentialsException("Invalid Username");
        }
        // check if the provided password matches the stored password
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid Password");
        }
        // if authentication is successful, return an authentication token with user details and authorities
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
