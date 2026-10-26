package com.example.questionbank.infrastructure.jpa;

import com.example.questionbank.domain.exception.NotFoundException;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.domain.repository.UserRepository;
import com.example.questionbank.infrastructure.jpa.mapper.UserMapper;
import com.example.questionbank.infrastructure.jpa.repository.SpringUserDataRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stores users with Spring Data JPA.
 */
@Repository
@Transactional(readOnly = true)
public class UserRepositoryJpa implements UserRepository {

    private final SpringUserDataRepository data;

    /**
     * Creates the repository.
     *
     * @param data the Spring Data interface for user records
     */
    public UserRepositoryJpa(SpringUserDataRepository data) {
        this.data = data;
    }

    /**
     * Inserts a new user. Users are immutable in the domain, so saving one that already has identity only
     * checks that it exists.
     */
    @Override
    @Transactional
    public User save(User user) {
        if (user.getId() == null) {
            return UserMapper.toDomain(data.save(UserMapper.toRecord(user)));
        }
        return findById(user.getId()).orElseThrow(() -> new NotFoundException("User not found: " + user.getId()));
    }

    @Override
    public Optional<User> findById(Long id) {
        return data.findById(id).map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return data.findByUsername(username).map(UserMapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return data.findAll(Sort.by("id")).stream().map(UserMapper::toDomain).toList();
    }
}
