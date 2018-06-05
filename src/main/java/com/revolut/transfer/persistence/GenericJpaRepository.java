package com.revolut.transfer.persistence;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class GenericJpaRepository<E, ID extends Serializable> {

    private EntityManager entityManager;

    private Class<E> entityClass;

    public GenericJpaRepository(Class<E> entityClass) {
        this.entityClass = entityClass;
        /*EntityManagerFactory factory = Persistence.createEntityManagerFactory("revolut-ds");
        entityManager = factory.createEntityManager();*/
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    protected Class<E> getEntityClass() {
        return entityClass;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public E findById(ID id) {
        return entityManager.find(getEntityClass(), id);
    }

    public List<E> findAll() {
        Query query = entityManager.createQuery("SELECT e FROM " + getEntityClass().getSimpleName() + " e");
        return query.getResultList();
    }

    public void deleteAll() {
        Query query = entityManager.createQuery("DELETE FROM " + getEntityClass().getSimpleName() + " e");
        query.executeUpdate();
    }

    public  E save(E t) {
        this.entityManager.persist(t);
        this.entityManager.flush();
        this.entityManager.refresh(t);
        return t;
    }

    public void deleteById(Object id) {
        E ref = this.entityManager.getReference(getEntityClass(), id);
        delete(ref);
    }

    public void delete(E entity) {
        this.entityManager.remove(entity);
    }

    public  E update(E t) {
        return this.entityManager.merge(t);
    }

    public void flush() {
        this.entityManager.flush();
    }

    public void startTransaction() {
        entityManager.getTransaction().begin();
    }

    public void commitTransaction() {
        entityManager.getTransaction().commit();
    }

    public void rollbackTransaction() {
        entityManager.getTransaction().rollback();
    }

    protected List<E> findWithNamedQuery(String namedQueryName){
        return this.entityManager.createNamedQuery(namedQueryName).getResultList();
    }

    protected List findWithNamedQuery(String namedQueryName, Object... parameters){
        Query query = this.entityManager.createNamedQuery(namedQueryName);
        for (int i = 0; i < parameters.length; i += 2) {
            query.setParameter(((String) parameters[i]), parameters[i + 1]);
        }
        return query.getResultList();
    }



}
