/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.picdrop.repository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author nvonstein
 */
public interface AwareRepository<ID, T, K> extends Repository<ID, T> {

    T save(T entity, K context);

    T get(ID id, K context);

    boolean delete(ID id, K context);

    T update(ID id, T entity, K context);

    List<T> list(K context);

    List<T> queryNamed(String qname, K context, Object... params) throws IOException;

    int deleteNamed(String qname, K context, Object... params) throws IOException;

    List<T> updateNamed(T entity, String qname, K context, Object... params) throws IOException;

    int updateNamed(Map<String, Object> flist, String qname, K context, Object... params) throws IOException;
}
