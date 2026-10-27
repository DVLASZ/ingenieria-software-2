package com.example.questionbank.application;

import com.example.questionbank.application.command.CreateUserCommand;
import com.example.questionbank.domain.exception.BusinessRuleException;
import com.example.questionbank.domain.exception.NotFoundException;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.domain.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use cases about users. It only knows the repository contract.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository users;

    /**
     * Creates the service.
     *
     * @param users the user repository contract
     */
    public UserService(UserRepository users) {
        this.users = users;
    }

    /**
     * Creates a user. The username must be free.
     *
     * @param command the data of the user
     * @return the stored user
     */
    public User create(CreateUserCommand command) {
        User user = new User(null, command.username(), command.fullName(), command.email(), command.role());
        if (users.findByUsername(user.getUsername()).isPresent()) {
            throw new BusinessRuleException("The username is already taken: " + user.getUsername());
        }
        return users.save(user);
    }

    /**
     * Finds a user.
     *
     * @param id the identity
     * @return the user
     */
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return users.findById(id).orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    /**
     * Lists all users.
     *
     * @return every user
     */
    @Transactional(readOnly = true)
    public List<User> list() {
        return users.findAll();
    }
}
