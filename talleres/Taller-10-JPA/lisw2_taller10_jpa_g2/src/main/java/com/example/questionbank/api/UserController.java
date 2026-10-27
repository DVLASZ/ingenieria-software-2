package com.example.questionbank.api;

import com.example.questionbank.api.dto.UserDtos.CreateUserRequest;
import com.example.questionbank.api.dto.UserDtos.UserResponse;
import com.example.questionbank.application.UserService;
import com.example.questionbank.application.command.CreateUserCommand;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP endpoints for users. It only translates HTTP and delegates to the service.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    /**
     * Creates the controller.
     *
     * @param service the user use cases
     */
    public UserController(UserService service) {
        this.service = service;
    }

    /**
     * Creates a user.
     *
     * @param request the data of the user
     * @return the stored user
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return UserResponse.from(service.create(
                new CreateUserCommand(request.username(), request.fullName(), request.email(), request.role())));
    }

    /**
     * Lists all users.
     *
     * @return every user
     */
    @GetMapping
    public List<UserResponse> list() {
        return service.list().stream().map(UserResponse::from).toList();
    }

    /**
     * Finds a user.
     *
     * @param id the identity
     * @return the user
     */
    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        return UserResponse.from(service.findById(id));
    }
}
