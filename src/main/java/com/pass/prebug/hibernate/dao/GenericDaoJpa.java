package com.pass.prebug.hibernate.dao;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.apache.commons.lang.StringUtils;

import com.pass.prebug.hibernate.database.BaseEntity;
import com.pass.prebug.input.AppProperties;


public class GenericDaoJpa <T extends BaseEntity<?>> {
	
	public enum MatchMode { START, END, EXACT, ANYWHERE }
	
	private static EntityManager entityManager;
	private AppProperties pb ;
	
	private static EntityManagerFactory emf;
	
	protected EntityManagerFactory getEntityManagerFactory(){
		if(emf == null ){
			pb = AppProperties.getInstance();
			String dbLocation = pb.getProperty("database.location");
			String persistenceUnit = pb.getProperty("database.persistenceUnitName", "derbyembedded");
			Map<String, Object> props = new HashMap<>();
			if(!StringUtils.isEmpty(dbLocation)){
				props.put("hibernate.connection.url", "jdbc:derby:" + dbLocation + ";create=true");
			}
			emf = Persistence.createEntityManagerFactory(persistenceUnit, props);
		}
		return emf;
	}
	/**
	 * Begin Transaction
	 * 
	 * @param 
	 * @return void
	 */
	public void beginTransaction() {
		
		entityManager = getEntityManagerFactory().createEntityManager();
		entityManager.getTransaction().begin();
   }

	/**
	 *Close Transaction
	 * 
	 * @param 
	 * @return void
	 * */
	public void endTransaction() {
        entityManager.getTransaction().commit();
		entityManager.close();
		getEntityManagerFactory().close();

	}
	/**
	 * Saves an entity.
	 * 
	 * @param entity
	 * @return newly created id for the entity.
	 */
	
	public void save(T entity) {
		entityManager.persist(entity);
		//return entity.getId();
	}

	public boolean isNew(T entity) {
		return !entityManager.contains(entity);
	}

	/**
	 * Marges objects with the same identifier within a session into a newly
	 * created object.
	 * 
	 * @param entity
	 * @return a newly created instance merged.
	 */
	public T merge(T entity) {
		return entityManager.merge(entity);
	}

	
	public T findByProperty(Class<T> clazz, String propertyName, Object value)  {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(clazz);
		Root<T> root = cq.from(clazz);
        cq.where(cb.equal(root.get(propertyName), value));
	    return  entityManager.createQuery(cq).getSingleResult();	
	}
	
	public List<T> findByWhereClause(Class<T> clazz, String propertyName)  {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(clazz);
		Root<T> root = cq.from(clazz);
        cq.where(cb.greaterThan(root.get(propertyName), 0));
	   return  entityManager.createQuery(cq).getResultList();	
	}
	public T findFirstEntry(Class<T> clazz, String propertyName)  {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(clazz);
		Root<T> root = cq.from(clazz);
		cq.orderBy(cb.asc(root.get(propertyName)));
        return  entityManager.createQuery(cq).setMaxResults(1).getSingleResult();	
	}	
	
	public T findLastEntry(Class<T> clazz, String propertyName) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(clazz);
		Root<T> root = cq.from(clazz);
		cq.orderBy(cb.desc(root.get(propertyName)));
        return entityManager.createQuery(cq).setMaxResults(1).getSingleResult();	
	}	
	
	public Long CountEntry(Class<T> clazz, String propertyName)  {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(clazz);
		Root<T> root = cq.from(clazz);
        cq.select((Selection) cb.count(root.get(propertyName)));
        Query query =  entityManager.createQuery(cq);
        Long result = (Long) query.getSingleResult();
        return result;
  	}	
		
		
	/**
	 * Find an entity by its identifier.
	 * 
	 * @param clazz
	 * @param id
	 * @return
	 */
	public  T find(Class<T> clazz, Serializable id) {
		return entityManager.find(clazz, id);
	}

	/**
	 * Finds an entity by one of its properties.
	 * 
	 * @param clazz the entity class.
	 * @param propertyName the property name.
	 * @param value the value by which to find.
	 * @return
	 */

	
	/**
	 * Finds all objects of an entity class.
	 * 
	 * @param clazz the entity class.
	 * @return
	 */
	public  List<T> findAll(Class<T> clazz) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(clazz);
		cq.from(clazz);
		return entityManager.createQuery(cq).getResultList();
	}
	

	

	public int UpdateTable(Class<T> clazz, String propertyName, Object oldValue,Object newValue) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaUpdate<T> cq = cb.createCriteriaUpdate(clazz);
		Root<T> root = cq.from(clazz);
		cq.set(root.get(propertyName), newValue);
		cq.where(cb.equal(root.get(propertyName), oldValue));
		return entityManager.createQuery(cq).executeUpdate();
	}
	
	/**
	 * Deletes tne entity.
	 * 
	 * @param clazz
	 * @param id
	 * @throws NotFoundException if the id does not exist.
	 */
//	public void delete(Class<T> clazz, PK id) {
//		T entity = find(clazz, id);
//		if (entity != null) {
//			entityManager.remove(entity);
//		} 
//	}


	
	
	
	/**
	 * Finds all objects of a class by the specified order.
	 * 
	 * @param clazz the entity class.
	 * @param order the order: ASC or DESC.
	 * @param propertiesOrder the properties on which to apply the ordering.
	 * 
	 * @return
	 */
//	public <T extends BaseEntity<?>> List<T> findAll(Class<T> clazz, Order order, String... propertiesOrder) {
//		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//		CriteriaQuery<T> cq = cb.createQuery(clazz);
//		Root<T> root = cq.from(clazz);
//		
//		List<javax.persistence.criteria.Order> orders = new ArrayList<javax.persistence.criteria.Order>();
//		for (String propertyOrder : propertiesOrder) {
//			if (order.isAscOrder()) {
//				orders.add(cb.asc(root.get(propertyOrder)));
//			} else {
//				orders.add(cb.desc(root.get(propertyOrder)));
//			}
//		}
//		cq.orderBy(orders);
//
//		return entityManager.createQuery(cq).getResultList();
//	}
//	
	
	/**
	 * Finds entities by a String property specifying a MatchMode. This search 
	 * is case insensitive.
	 * 
	 * @param clazz the entity class.
	 * @param propertyName the property name.
	 * @param value the value to check against.
	 * @param matchMode the match mode: EXACT, START, END, ANYWHERE.
	 * @return
	 */
//	public <T extends BaseEntity<?>> List<T> findByProperty(Class<T> clazz, String propertyName, String value, MatchMode matchMode) {
//		//convert the value String to lowercase
//		value = value.toLowerCase();
//		if (MatchMode.START.equals(matchMode)) {
//			value = value + "%";
//		} else if (MatchMode.END.equals(matchMode)) {
//			value = "%" + value;
//		} else if (MatchMode.ANYWHERE.equals(matchMode)) {
//			value = "%" + value + "%";
//		}
//		
//		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//		CriteriaQuery<T> cq = cb.createQuery(clazz);
//		Root<T> root = cq.from(clazz);
//		cq.where(cb.like(cb.lower(root.get(propertyName)), value));
//		
//		return entityManager.createQuery(cq).getResultList();
//	}	
//	
	

}
