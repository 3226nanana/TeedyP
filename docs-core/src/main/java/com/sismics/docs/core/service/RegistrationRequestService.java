package com.sismics.docs.core.service;

import com.sismics.docs.core.dao.RegistrationRequestDao;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.model.jpa.RegistrationRequest;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.constant.Constants;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ServerException;
import java.util.Date;
import java.util.Random;

/**
 * 注册请求服务类：提供访客提交注册请求和管理员审核处理逻辑。
 */
public class RegistrationRequestService {
    private RegistrationRequestDao requestDao = new RegistrationRequestDao();
    private UserDao userDao = new UserDao();

    /**
     * 访客提交注册请求。
     *
     * @param email 邮箱
     * @param fullname 姓名
     * @param message 附加说明（可选）
     * @return 请求 ID
     * @throws ClientException 如果邮箱已存在对应账户
     */
    public String submitRequest(String email, String fullname, String message) {
        // 检查邮箱是否已被注册为用户
        User existingUser = userDao.getActiveByUsername(email);
        if (existingUser != null) {
            // 如果用户名（邮箱）已存在，拒绝提交
            throw new ClientException("AlreadyExistingUsername", "邮箱已被使用");
        }
        // 创建并保存注册请求
        RegistrationRequest request = new RegistrationRequest()
                .setEmail(email)
                .setFullname(fullname)
                .setMessage(message)
                .setStatus("PENDING")
                .setCreatedDate(new Date());
        requestDao.create(request);
        return request.getId();
    }

    /**
     * 管理员接受注册请求：创建用户账号并返回初始密码。
     *
     * @param requestId 注册请求 ID
     * @return 新账户的随机初始密码
     * @throws ClientException 如果请求不存在、已被处理，或用户名冲突
     */
    public String acceptRequest(String requestId) {
        // 获取请求
        RegistrationRequest request = requestDao.getById(requestId);
        if (request == null) {
            throw new ClientException("UnknownRequest", "注册请求不存在");
        }
        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            throw new ClientException("AlreadyProcessed", "该注册请求已处理");
        }
        // 准备创建新用户
        String email = request.getEmail();
        String username = email; // 使用邮箱作为用户名
        String password = generateRandomPassword(); // 生成随机初始密码
        // 设置用户属性并创建
        User user = new User();
        user.setRoleId(Constants.DEFAULT_USER_ROLE);
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        // 设置默认存储配额（字节）。这里设置为1GB，如有需要可修改或从配置读取。
        user.setStorageQuota(1073741824L);
        user.setOnboarding(true);
        try {
            userDao.create(user, null);
        } catch (Exception e) {
            if ("AlreadyExistingUsername".equals(e.getMessage())) {
                // 用户名冲突
                throw new ClientException("AlreadyExistingUsername", "登录名已被使用", e);
            } else {
                throw new ServerException("UnknownError", "创建用户时发生未知错误", e);
            }
        }
        // 更新注册请求状态为 accepted
        request.setStatus("ACCEPTED");
        requestDao.create(request); // Persist update (or use em.merge in real case)
        return password;
    }

    /**
     * 管理员拒绝注册请求。
     *
     * @param requestId 注册请求 ID
     * @throws ClientException 如果请求不存在或已被处理
     */
    public void rejectRequest(String requestId) {
        RegistrationRequest request = requestDao.getById(requestId);
        if (request == null) {
            throw new ClientException("UnknownRequest", "注册请求不存在");
        }
        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            throw new ClientException("AlreadyProcessed", "该注册请求已处理");
        }
        // 更新状态为 rejected
        request.setStatus("REJECTED");
        requestDao.create(request);
    }

    /**
     * 生成一个随机初始密码，包含字母数字，长度8位。
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}