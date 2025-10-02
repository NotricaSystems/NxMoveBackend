package com.next.move.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GooglePublicKeysManager;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class LoginService {
    private static final String GOOGLE_CLIENT_ID = "1082527571682-2lm7ku45vuq1baaouake6eqkq1cai7rh.apps.googleusercontent.com";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static GoogleIdToken.Payload verifyWithGoogle(String idTokenString) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                JSON_FACTORY)  // <-- use GsonFactory here
                .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            return idToken.getPayload();
        } else {
            throw new RuntimeException("Invalid ID token.");
        }
    }

    public GoogleIdToken.Payload handleGoogleLogin(String tokenFromFrontend) throws Exception {

        /*
        String userId = payload.getSubject(); // Google's unique user ID
        String email = payload.getEmail();
        boolean emailVerified = payload.getEmailVerified();
        String name = (String) payload.get("name");
        String givenName = (String) payload.get("given_name");
        String pictureUrl = (String) payload.get("picture");
                 */
        return verifyWithGoogle(tokenFromFrontend);
    }
}
