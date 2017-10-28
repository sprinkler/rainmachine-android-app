package com.rainmachine.infrastructure;

import com.rainmachine.R;
import com.rainmachine.domain.boundary.data.SprinklerPrefRepository;
import com.rainmachine.domain.util.Strings;
import com.rainmachine.presentation.util.Toasts;

public class SprinklerUtils {

    private SprinklerPrefRepository sprinklerPrefsRepository;

    public SprinklerUtils(SprinklerPrefRepository sprinklerPrefsRepository) {
        this.sprinklerPrefsRepository = sprinklerPrefsRepository;
    }

    public boolean isAuthenticated() {
        return !Strings.isBlank(sprinklerPrefsRepository.sessionCookie());
    }

    public void dealWithSessionExpiration() {
        logout();
        InfrastructureUtils.finishAllSprinklerActivities();
        Toasts.show(R.string.all_session_expired);
    }

    public void logout() {
        sprinklerPrefsRepository.clearSessionCookie();
    }
}
