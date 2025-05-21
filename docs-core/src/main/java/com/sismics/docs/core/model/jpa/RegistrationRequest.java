package com.sismics.docs.core.model.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;

/**
 * 注册请求实体类 (Registration Request Entity).
 * 包含访客提交的注册请求信息，如邮箱、姓名、附加说明、状态和创建日期。
 */
@Entity
@Table(name = "T_REGISTRATION_REQUEST")
public class RegistrationRequest {
    /**
     * 请求 ID（主键）。
     */
    @Id
    @Column(name = "RRE_ID_C", length = 36)
    private String id;

    /**
     * 注册邮箱（用户名将使用该邮箱）。
     */
    @Column(name = "RRE_EMAIL_C", nullable = false, length = 100)
    private String email;

    /**
     * 全名。
     */
    @Column(name = "RRE_FULLNAME_C", nullable = false, length = 200)
    private String fullname;

    /**
     * 附加说明信息。
     */
    @Column(name = "RRE_MESSAGE_C", length = 1000)
    private String message;

    /**
     * 请求状态：pending（待处理），accepted（已接受），rejected（已拒绝）。
     */
    @Column(name = "RRE_STATUS_C", nullable = false, length = 20)
    private String status;

    /**
     * 创建日期。
     */
    @Column(name = "RRE_CREATEDATE_D", nullable = false)
    private Date createdDate;

    // Getter 和 Setter 方法

    public String getId() {
        return id;
    }

    public RegistrationRequest setId(String id) {
        this.id = id;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public RegistrationRequest setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getFullname() {
        return fullname;
    }

    public RegistrationRequest setFullname(String fullname) {
        this.fullname = fullname;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public RegistrationRequest setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public RegistrationRequest setStatus(String status) {
        this.status = status;
        return this;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public RegistrationRequest setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
        return this;
    }
}
