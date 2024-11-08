package jpa;

import persistence.sql.entity.EntityJoin;

import java.util.Collection;

public class EntityManagerImpl implements EntityManager {
    private final EntityPersister entityPersister;
    private final PersistenceContext persistenceContext;
    private final EntityLoader entityLoader;

    public EntityManagerImpl(EntityPersister entityPersister, EntityLoader entityLoader) {
        this.entityPersister = entityPersister;
        this.entityLoader = entityLoader;
        this.persistenceContext = new PersistenceContextImpl();
    }

    @Override
    public <T> T find(Class<T> clazz, Long id) {
        T entityPersistenceContext = persistenceContext.get(clazz, id);
        if (entityPersistenceContext != null) {
            return entityPersistenceContext;
        }

        T entity = entityLoader.find(clazz, id);

        persistenceContext.add(entity);
        persistenceContext.createDatabaseSnapshot(entity);
        return entity;
    }

    @Override
    public <T> T persist(T entity) {
        persistenceContext.addEntityEntry(entity, new EntityEntry(EntityStatus.SAVING));
        T insertedEntity = entityPersister.insert(entity);
        persistenceContext.add(entity);
        persistenceContext.createDatabaseSnapshot(entity);

        persistJoinEntity(entity);

        return insertedEntity;
    }

    private <T> void persistJoinEntity(T entity) {
        EntityJoin entityJoin = new EntityJoin(entity.getClass());
        entityJoin.getEntityJoinInfos().forEach(entityJoinInfo -> {
            Collection<?> entityJoinCollections = entityJoinInfo.getEntityJoinCollections(entity);
            entityJoinCollections.forEach(this::persist);
        });
    }

    @Override
    public void merge(Object entity) {
        if (persistenceContext.isDirty(entity)) {
            persistenceContext.add(entity);
        }
    }

    @Override
    public void flush() {
        for (Object entity : persistenceContext.getDirtyEntities()) {
            entityPersister.update(entity);
            persistenceContext.addEntityEntry(entity, new EntityEntry(EntityStatus.GONE));
        }
    }

    @Override
    public void remove(Object entity) {
        entityPersister.delete(entity);
        persistenceContext.remove(entity);
        persistenceContext.removeDatabaseSnapshot(entity);
    }

}
