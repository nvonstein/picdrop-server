/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.picdrop.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.nimbusds.jwt.JWTClaimsSet;
import com.picdrop.exception.ApplicationException;
import com.picdrop.guice.factory.CookieProviderFactory;
import com.picdrop.model.RequestContext;
import com.picdrop.model.TokenSet;
import com.picdrop.model.user.RegisteredUser;
import com.picdrop.model.user.User;
import com.picdrop.repository.Repository;
import com.picdrop.security.authentication.Permission;
import com.picdrop.security.authentication.authenticator.Authenticator;
import com.picdrop.security.token.ClaimSetFactory;
import com.picdrop.security.token.WebTokenFactory;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 *
 * @author i330120
 */
@Path("/app")
public class AuthorizationService {

    Repository<String, RegisteredUser> userRepo;
    Repository<String, TokenSet> tsRepo;

    CookieProviderFactory cookieProvFactory;
    WebTokenFactory tokenFactory;

    Authenticator<RegisteredUser> basicAuthenticator;
    Authenticator<RegisteredUser> refreshAuthenticator;

    ClaimSetFactory<RegisteredUser> authCsFact;
    ClaimSetFactory<RegisteredUser> refreshCsFact;

    @Inject
    Provider<RequestContext> contextProv;

    @Inject
    ObjectMapper mapper;

    final boolean cookieEnabled;
    final int tsExpiry;

    @Inject
    public AuthorizationService(
            Repository<String, RegisteredUser> userRepo,
            Repository<String, TokenSet> tsRepo,
            CookieProviderFactory cookieProvFactory,
            WebTokenFactory tokenFactory,
            @Named("authenticator.basic") Authenticator<RegisteredUser> basicAuthenticator,
            @Named("authenticator.token.refresh") Authenticator<RegisteredUser> refreshAuthenticator,
            @Named("claimset.factory.auth") ClaimSetFactory<RegisteredUser> authCsFact,
            @Named("claimset.factory.refresh") ClaimSetFactory<RegisteredUser> refreshCsFact,
            @Named("service.session.cookie.enabled") boolean cookieEnabled,
            @Named("service.session.jwt.refresh.exp") int tsExpiry) {
        this.tsRepo = tsRepo;
        this.userRepo = userRepo;
        this.cookieProvFactory = cookieProvFactory;
        this.tokenFactory = tokenFactory;
        this.authCsFact = authCsFact;
        this.refreshCsFact = refreshCsFact;
        this.basicAuthenticator = basicAuthenticator;
        this.refreshAuthenticator = refreshAuthenticator;
        this.cookieEnabled = cookieEnabled;
        this.tsExpiry = tsExpiry;
    }

    protected TokenSet.JsonWrapper generateTokens(
            RegisteredUser user,
            String nonce) throws ApplicationException {
        JWTClaimsSet authClaims = this.authCsFact.builder()
                .subject(user.getId())
                .build();

        JWTClaimsSet refreshClaims = this.refreshCsFact.builder()
                .subject(user.getId())
                .build();

        TokenSet ts = new TokenSet();
        ts.setAuthJti(authClaims.getJWTID());
        ts.setRefreshJti(refreshClaims.getJWTID());
        ts.setOwner(user);
        ts.setExpireAt(DateTime.now(DateTimeZone.UTC)
                .plusMinutes(tsExpiry)
                .toDate());

        ts = this.tsRepo.save(ts);

        try {
            String authToken = tokenFactory.getToken(authClaims);
            String refreshToken = tokenFactory.getToken(refreshClaims);

            return new TokenSet.JsonWrapper()
                    .auth(authToken)
                    .refresh(refreshToken)
                    .nonce(Strings.isNullOrEmpty(nonce) ? null : nonce);
        } catch (IOException ex) {
            throw new ApplicationException(ex)
                    .status(403);
        }
    }

    @POST
    @Path("/login")
    public Response loginUser(@Context HttpServletRequest request,
            @QueryParam("nonce") String nonce) throws ApplicationException { // TODO make redirect target injectable
        RegisteredUser user = basicAuthenticator.authenticate(request);
        if (user == null) {
            throw new ApplicationException()
                    .status(403);
        }

        TokenSet.JsonWrapper tokens = generateTokens(user, nonce);

        user.setLastLogin();
        userRepo.update(user.getId(), user);

        NewCookie c = cookieProvFactory.getSessionCookieProvider(tokens.getAuth()).get();

        return Response
                .ok(tokens, MediaType.APPLICATION_JSON)
                .cookie(c)
                .build();
    }

    @POST
    @Path("/refresh")
    public Response refreshToken(@Context HttpServletRequest request,
            @QueryParam("nonce") String nonce) throws ApplicationException {
        RegisteredUser user = refreshAuthenticator.authenticate(request);
        if (user == null) {
            throw new ApplicationException()
                    .status(403);
        }
        
        tsRepo.delete(user.getActiveToken().getId());

        TokenSet.JsonWrapper tokens = generateTokens(user, nonce);

        user.setLastLogin();
        userRepo.update(user.getId(), user);

        NewCookie c = cookieProvFactory.getSessionCookieProvider(tokens.getAuth()).get();

        return Response
                .ok(tokens, MediaType.APPLICATION_JSON)
                .cookie(c)
                .build();
    }

    @POST
    @Path("/logout")
    @Permission("*/logout")
    public Response logoutUser() { // TODO rework login/logout
        User user = contextProv.get().getPrincipal();
        if (user == null) {
            return Response.ok().build();
        }

        RegisteredUser ru = user.to(RegisteredUser.class);
        tsRepo.delete(ru.getActiveToken().getId());

        // generate kill cookie
        NewCookie c = cookieProvFactory.getSessionCookieProvider("").get();
        NewCookie killcookie = new NewCookie(c, c.getComment(), 0, c.isSecure());

        return Response.ok().cookie(killcookie).build();
    }
}
