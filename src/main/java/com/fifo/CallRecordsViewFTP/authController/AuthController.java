package com.fifo.CallRecordsViewFTP.authController;

import com.fifo.CallRecordsViewFTP.authDTO.JwtResponse;
import com.fifo.CallRecordsViewFTP.authDTO.LoginRequest;
import com.fifo.CallRecordsViewFTP.authDTO.MessageResponse;
import com.fifo.CallRecordsViewFTP.authDTO.UserDto;
import com.fifo.CallRecordsViewFTP.authModel.User;
import com.fifo.CallRecordsViewFTP.authRepository.RoleRepository;
import com.fifo.CallRecordsViewFTP.authRepository.UserRepository;
import com.fifo.CallRecordsViewFTP.authService.UserService;
import com.fifo.CallRecordsViewFTP.security.jwt.JwtUtils;
import com.fifo.CallRecordsViewFTP.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://43.231.78.77:5010", allowCredentials = "true")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        String token = jwtUtils.generateJwtToken(userDetails);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new JwtResponse(token));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDto userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(userDto.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(userDto.getUsername(),
                userDto.getEmail(),
                encoder.encode(userDto.getPassword()),
                userDto.getUserFirstName(),
                userDto.getUserLastName());

        userDto.setRoles(Collections.singleton("ADMIN"));
        try {
            userService.create(userDto);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new MessageResponse("one of role not found"));
        }

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }



    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("You've been signed out!"));
    }


    @PostMapping("/client/signup")
    public ResponseEntity<?> registerClient(@Valid @RequestBody UserDto userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(userDto.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(userDto.getUsername(),
                userDto.getEmail(),
                encoder.encode(userDto.getPassword()),
                userDto.getUserFirstName(),
                userDto.getUserLastName());

        userDto.setRoles(Collections.singleton("Client"));
        try {
            userService.create(userDto);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new MessageResponse("one of role not found"));
        }

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

}
