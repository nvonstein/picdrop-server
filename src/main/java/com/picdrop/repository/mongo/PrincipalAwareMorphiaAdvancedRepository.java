/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.picdrop.repository.mongo;

import com.mongodb.DBObject;
import com.picdrop.model.user.User;
import com.picdrop.repository.AwareAdvancedRepository;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateResults;

/**
 *
 * @author nvonstein
 */
public class PrincipalAwareMorphiaAdvancedRepository<T> extends PrincipalAwareMorphiaRepository<T> implements AwareAdvancedRepository<String, T, User> {

    public PrincipalAwareMorphiaAdvancedRepository(Datastore ds, Class<T> entityType) {
        super(ds, entityType);
    }

    @Override
    public int deleteNamed(String qname, User context, Object... params) throws IOException {
        DBObject dbObj = compileQuery(qname, params);

        if (context != null) {
            dbObj = addPrincipalClause(dbObj, context);
        }

        Query<T> query = ds.getQueryFactory().createQuery(ds, ds.getCollection(entityType), entityType, dbObj);

        return ds.delete(query).getN();
    }

    @Override
    public List<T> updateNamed(T entity, String qname, User context, Object... params) throws IOException {
        DBObject dbObj = compileQuery(qname, params);

        if (context != null) {
            dbObj = addPrincipalClause(dbObj, context);
        }

        Query<T> query = ds.getQueryFactory().createQuery(ds, ds.getCollection(entityType), entityType, dbObj);

        UpdateResults ur = ds.updateFirst(query, entity, false);
        return Arrays.asList();
    }

    @Override
    public int deleteNamed(String qname, Object... params) throws IOException {
        DBObject dbObj = compileQuery(qname, params);

        dbObj = addPrincipalClause(dbObj);

        Query<T> query = ds.getQueryFactory().createQuery(ds, ds.getCollection(entityType), entityType, dbObj);

        return ds.delete(query).getN();
    }

    @Override
    public List<T> updateNamed(T entity, String qname, Object... params) throws IOException {
        DBObject dbObj = compileQuery(qname, params);

        dbObj = addPrincipalClause(dbObj);

        Query<T> query = ds.getQueryFactory().createQuery(ds, ds.getCollection(entityType), entityType, dbObj);

        UpdateResults ur = ds.updateFirst(query, entity, false);
        return Arrays.asList();
    }

}