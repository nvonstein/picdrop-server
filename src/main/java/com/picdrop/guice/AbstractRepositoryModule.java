/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.picdrop.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.picdrop.guice.names.Config;
import com.picdrop.guice.names.Queries;
import com.picdrop.model.Share;
import com.picdrop.model.ShareReference;
import com.picdrop.model.TokenSet;
import com.picdrop.model.TokenSetReference;
import com.picdrop.model.resource.Collection;
import com.picdrop.model.resource.CollectionReference;
import com.picdrop.model.resource.FileResource;
import com.picdrop.model.resource.FileResourceReference;
import com.picdrop.model.user.RegisteredUser;
import com.picdrop.model.user.RegisteredUserReference;
import com.picdrop.model.user.User;
import com.picdrop.repository.AdvancedRepository;
import com.picdrop.repository.AwareAdvancedRepository;
import com.picdrop.repository.AwareRepository;
import com.picdrop.repository.Repository;
import com.picdrop.repository.mongo.MorphiaAdvancedRepository;
import com.picdrop.repository.mongo.MorphiaRepository;
import com.picdrop.repository.mongo.NamedQueries;
import com.picdrop.repository.mongo.PrincipalAwareMorphiaAdvancedRepository;
import com.picdrop.repository.mongo.RepositoryPrototype;
import java.util.Map;
import java.util.Properties;
import org.mongodb.morphia.Datastore;

/**
 *
 * @author nvonstein
 */
public abstract class AbstractRepositoryModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(new TypeLiteral<Map<String, String>>() {
        }).annotatedWith(Queries.class).toInstance(NamedQueries.getQueries());
        // Registered user repo
        bindRegisteredUserRepo(binder);
        // Resource repo
        bindResourceRepo(binder);
        // Collections repo
        bindCollectionsRepo(binder);
        // Collectionitem repo
        bindCollectionItemRepo(binder);
        // Share repo
        bindShareRepo(binder);
        // TokenSet repo
        bindTokenSetRepo(binder);
        // Static bindings
        bindStaticRepoReferences(binder);
    }

    protected void bindRegisteredUserRepo(Binder binder) {
        binder.bind(new TypeLiteral<AdvancedRepository<String, RegisteredUser>>() {
        }).to(new TypeLiteral<MorphiaAdvancedRepository<RegisteredUser>>() {
        }).in(Singleton.class);
        binder.bind(new TypeLiteral<Repository<String, RegisteredUser>>() {
        }).to(new TypeLiteral<MorphiaAdvancedRepository<RegisteredUser>>() {
        }).in(Singleton.class);
    }

    protected void bindResourceRepo(Binder binder) {
        binder.bind(new TypeLiteral<Repository<String, FileResource>>() {
        }).to(new TypeLiteral<PrincipalAwareMorphiaAdvancedRepository<FileResource>>() {
        }).in(Singleton.class);
        binder.bind(new TypeLiteral<AwareRepository<String, FileResource, User>>() {
        }).to(new TypeLiteral<PrincipalAwareMorphiaAdvancedRepository<FileResource>>() {
        }).in(Singleton.class);
        binder.bind(new TypeLiteral<AdvancedRepository<String, FileResource>>() {
        }).to(new TypeLiteral<PrincipalAwareMorphiaAdvancedRepository<FileResource>>() {
        }).in(Singleton.class);
        binder.bind(new TypeLiteral<AwareAdvancedRepository<String, FileResource, User>>() {
        }).to(new TypeLiteral<PrincipalAwareMorphiaAdvancedRepository<FileResource>>() {
        }).in(Singleton.class);
    }

    protected void bindCollectionsRepo(Binder binder) {
        binder.bind(new TypeLiteral<Repository<String, Collection>>() {
        }).to(new TypeLiteral<PrincipalAwareMorphiaAdvancedRepository<Collection>>() {
        }).in(Singleton.class);
        binder.bind(new TypeLiteral<AwareRepository<String, Collection, User>>() {
        }).to(new TypeLiteral<PrincipalAwareMorphiaAdvancedRepository<Collection>>() {
        }).in(Singleton.class);
        binder.bind(new TypeLiteral<AdvancedRepository<String, Collection>>() {
        }).to(new TypeLiteral<PrincipalAwareMorphiaAdvancedRepository<Collection>>() {
        }).in(Singleton.class);
        binder.bind(new TypeLiteral<AwareAdvancedRepository<String, Collection, User>>() {
        }).to(new TypeLiteral<PrincipalAwareMorphiaAdvancedRepository<Collection>>() {
        }).in(Singleton.class);
    }

    protected void bindCollectionItemRepo(Binder binder) {
        binder.bind(new TypeLiteral<Repository<String, Collection.CollectionItem>>() {
        }).to(new TypeLiteral<MorphiaAdvancedRepository<Collection.CollectionItem>>() {
        }).in(Singleton.class);
        binder.bind(new TypeLiteral<AdvancedRepository<String, Collection.CollectionItem>>() {
        }).to(new TypeLiteral<MorphiaAdvancedRepository<Collection.CollectionItem>>() {
        }).in(Singleton.class);
    }

    protected void bindShareRepo(Binder binder) {
        binder.bind(new TypeLiteral<Repository<String, Share>>() {
        }).to(new TypeLiteral<PrincipalAwareMorphiaAdvancedRepository<Share>>() {
        }).in(Singleton.class);
        binder.bind(new TypeLiteral<AwareRepository<String, Share, User>>() {
        }).to(new TypeLiteral<PrincipalAwareMorphiaAdvancedRepository<Share>>() {
        }).in(Singleton.class);
        binder.bind(new TypeLiteral<AdvancedRepository<String, Share>>() {
        }).to(new TypeLiteral<PrincipalAwareMorphiaAdvancedRepository<Share>>() {
        }).in(Singleton.class);
        binder.bind(new TypeLiteral<AwareAdvancedRepository<String, Share, User>>() {
        }).to(new TypeLiteral<PrincipalAwareMorphiaAdvancedRepository<Share>>() {
        }).in(Singleton.class);
    }

    protected void bindTokenSetRepo(Binder binder) {
        binder.bind(new TypeLiteral<Repository<String, TokenSet>>() {
        }).to(new TypeLiteral<MorphiaAdvancedRepository<TokenSet>>() {
        }).in(Singleton.class);
        binder.bind(new TypeLiteral<AdvancedRepository<String, TokenSet>>() {
        }).to(new TypeLiteral<MorphiaAdvancedRepository<TokenSet>>() {
        }).in(Singleton.class);
    }

    protected void bindStaticRepoReferences(Binder binder) {
        binder.requestStaticInjection(CollectionReference.class);
        binder.requestStaticInjection(FileResourceReference.class);
        binder.requestStaticInjection(ShareReference.class);
        binder.requestStaticInjection(RegisteredUserReference.class);
        binder.requestStaticInjection(Collection.CollectionItemReference.class);
        binder.requestStaticInjection(TokenSetReference.class);
    }

    protected abstract MongoDatabase provideDatabase(MongoClient client);

    protected abstract Datastore provideDatastore(MongoClient client);

    protected abstract MongoClient provideMongoClient(@Config Properties config);
    
    protected abstract RepositoryPrototype provideRepositoryPrototype(Datastore ds, ObjectMapper mapper, Map<String, String> queries);

    protected abstract AdvancedRepository<String, TokenSet> provideTokenSetRepo(RepositoryPrototype prototype);

    protected abstract AdvancedRepository<String, Collection.CollectionItem> provideCollectionItemRepo(RepositoryPrototype prototype);

    protected abstract AdvancedRepository<String, RegisteredUser> provideRegisteredUserRepo(RepositoryPrototype prototype);

    protected abstract AwareAdvancedRepository<String, Collection, User> provideCollectionRepo(RepositoryPrototype prototype);

    protected abstract AwareAdvancedRepository<String, FileResource, User> provideResourceRepo(RepositoryPrototype prototype);

    protected abstract AwareAdvancedRepository<String, Share, User> provideShareRepo(RepositoryPrototype prototype);

}
