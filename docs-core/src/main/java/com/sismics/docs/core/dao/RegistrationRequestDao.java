
package com.sismics.docs.core.dao;

import com.sismics.docs.core.model.jpa.RegistrationRequest;
import com.sismics.util.context.ThreadLocalContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;

import java.util.List;
import java.util.UUID;

/**
 * RegistrationRequest DAO，用于访问和管理注册请求数据。
 */
public class RegistrationRequestDao {
    /**
     * 创建新的注册请求。
     *
     * @param request 注册请求实体对象
     * @return 请求 ID
     */
    public String create(RegistrationRequest request) {
        // 生成唯一ID
        request.setId(UUID.randomUUID().toString());
        request.setCreatedDate(request.getCreatedDate() != null ? request.getCreatedDate() : new java.util.Date());
        // 持久化实体
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(request);
        return request.getId();
    }

    /**
     * 根据 ID 获取注册请求。
     *
     * @param id 请求 ID
     * @return 对应的 RegistrationRequest 或 null
     */
    public RegistrationRequest getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        return em.find(RegistrationRequest.class, id);
    }

    /**
     * 获取所有注册请求，按创建日期倒序排列。
     *
     * @return 注册请求列表
     */
    @SuppressWarnings("unchecked")
    public List<RegistrationRequest> findAll() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("SELECT r FROM RegistrationRequest r ORDER BY r.createdDate DESC");
        return q.getResultList();
    }

    /**
     * 根据邮箱查找最新的注册请求。
     *
     * @param email 邮箱地址
     * @return 最近的 RegistrationRequest 或 null
     */
    public RegistrationRequest getLatestByEmail(String email) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("SELECT r FROM RegistrationRequest r WHERE r.email = :email ORDER BY r.createdDate DESC");
        q.setParameter("email", email);
        q.setMaxResults(1);
        try {
            return (RegistrationRequest) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
