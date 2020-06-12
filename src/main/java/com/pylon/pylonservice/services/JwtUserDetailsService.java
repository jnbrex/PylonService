package com.pylon.pylonservice.services;


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.pylon.pylonservice.exceptions.UserIdNotFoundException;
import com.pylon.pylonservice.model.tables.User;
import com.pylon.pylonservice.model.tables.UsernameUser;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Log4j2
@Service
public class JwtUserDetailsService implements UserDetailsService {
    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        final UsernameUser usernameUser = dynamoDBMapper.load(UsernameUser.class, username);
        if (usernameUser == null) {
            final String message = String.format("User not found with username %s", username);
            log.error(message);
            throw new UsernameNotFoundException(message);
        }

        final String userId = usernameUser.getUserId();
        final User user = dynamoDBMapper.load(User.class, userId);
        if (user == null) {
            final String message = String.format("No User data found for user with username %s", username);
            log.error(message);
            throw new IllegalStateException(message);
        }

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            new ArrayList<>()
        );
    }

    public UserDetails loadUserByUserId(final String userId) throws UsernameNotFoundException {
        final User user = dynamoDBMapper.load(User.class, userId);

        if (user == null) {
            final String message = String.format("User not found with userId %s", userId);
            log.error(message);
            throw new UserIdNotFoundException(message);
        }

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            new ArrayList<>()
        );
    }
}
