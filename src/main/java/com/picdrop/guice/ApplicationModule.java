/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.picdrop.guice;

import com.picdrop.guice.factory.CookieProviderFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.picdrop.guice.provider.CookieProvider;
import com.picdrop.guice.provider.JWETokenDirectEncrypterDecrypterProvider;
import com.picdrop.guice.provider.JWSTokenMACSignerVerifierProvider;
import com.picdrop.model.RequestContext;
import com.picdrop.guice.provider.SessionCookieProvider;
import com.picdrop.helper.EnvHelper;
import com.picdrop.security.authentication.authenticator.Authenticator;
import com.picdrop.security.authentication.authenticator.BasicAuthenticator;
import com.picdrop.security.authentication.authenticator.TokenAuthenticator;
import com.picdrop.security.token.WebTokenFactory;
import com.picdrop.security.token.WebTokenFactoryImpl;
import com.picdrop.security.token.cipher.TokenCipher;
import com.picdrop.security.token.cipher.TokenCipherImpl;
import com.picdrop.security.token.signer.TokenSigner;
import com.picdrop.security.token.signer.TokenSignerImpl;
import com.picdrop.service.filter.AuthenticationFilter;
import com.picdrop.service.implementation.AuthorizationService;
import com.picdrop.service.implementation.CollectionService;
import com.picdrop.service.implementation.GroupService;
import com.picdrop.service.implementation.RegisteredUserService;
import com.picdrop.service.implementation.FileResourceService;
import com.picdrop.service.implementation.UserService;
import javax.inject.Singleton;
import org.jboss.resteasy.plugins.guice.RequestScoped;

/**
 *
 * @author i330120
 */
public class ApplicationModule implements Module {

    @Override
    public void configure(Binder binder) {
        // Services
        bindServices(binder);

        // Json
        bindObjectMapper(binder);

        // Environment
        bindProperties(binder);
    }
    
    protected void bindProperties(Binder binder) {
        Names.bindProperties(binder, EnvHelper.getProperties());
    }
    
    protected void bindObjectMapper(Binder binder) {
        binder.bind(ObjectMapper.class).toInstance(new ObjectMapper());
    }
    
    protected void bindServices(Binder binder) {
        binder.bind(UserService.class).in(Singleton.class);
        binder.bind(GroupService.class).in(Singleton.class);
        binder.bind(FileResourceService.class).in(Singleton.class);
        binder.bind(RegisteredUserService.class).in(Singleton.class);
        binder.bind(AuthorizationService.class).in(Singleton.class);
        binder.bind(CollectionService.class).in(Singleton.class);
    }
}
