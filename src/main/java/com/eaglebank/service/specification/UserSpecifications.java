package com.eaglebank.service.specification;

import com.eaglebank.domain.User;
import com.eaglebank.web.exception.DomainExceptions.ForbiddenOperationException;

public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<User> isOwnedBy(String authenticatedUserId) {
        return user -> user != null && user.getId().equals(authenticatedUserId);
    }

    public static void assertOwnedBy(User user, String authenticatedUserId, String forbiddenMessage) {
        if (!isOwnedBy(authenticatedUserId).isSatisfiedBy(user)) {
            throw new ForbiddenOperationException(forbiddenMessage);
        }
    }
}
