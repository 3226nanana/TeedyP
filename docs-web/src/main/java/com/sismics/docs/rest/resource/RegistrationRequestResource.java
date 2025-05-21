package com.sismics.docs.rest.resource;

import com.sismics.docs.core.service.RegistrationRequestService;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ClientException;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.StringReader;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

/**
 * 注册请求 REST 资源 (Registration Request REST Resource).
 * 提供访客提交注册请求以及管理员查看、审核请求的API接口。
 */
@Path("/registration-request")
public class RegistrationRequestResource extends BaseResource {
    private RegistrationRequestService requestService = new RegistrationRequestService();

    /**
     * 访客提交注册请求。
     *
     * @api {post} /registration-request 提交注册请求
     * @apiName PostRegistrationRequest
     * @apiGroup RegistrationRequest
     * @apiParam {String} email 邮箱
     * @apiParam {String} fullname 姓名
     * @apiParam {String} message 附加说明
     * @apiSuccess {String} status 状态 OK
     * @apiError (client) ValidationError 参数验证失败
     * @apiError (client) AlreadyExistingUsername 邮箱已被使用
     * @apiVersion 1.0.0
     *
     * @param email 邮箱
     * @param fullname 姓名
     * @param message 附加说明
     * @return Response
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response submit(@FormParam("email") String email,
                           @FormParam("fullname") String fullname,
                           @FormParam("message") String message) {
        // Note: 不需要认证，访客可访问
        email = email != null ? email.trim() : null;
        fullname = fullname != null ? fullname.trim() : null;
        // 简单验证
        if (email == null || fullname == null || email.isEmpty() || fullname.isEmpty()) {
            throw new ClientException("ValidationError", "邮箱和姓名为必填项");
        }
        // 执行提交逻辑
        requestService.submitRequest(email, fullname, message);
        JsonObjectBuilder response = Json.createObjectBuilder().add("status", "ok");
        return Response.ok(response.build()).build();
    }

    /**
     * 管理员获取所有注册请求列表。
     *
     * @api {get} /registration-request 获取注册请求列表
     * @apiName GetRegistrationRequestList
     * @apiGroup RegistrationRequest
     * @apiSuccess {Object[]} requests 注册请求列表
     * @apiSuccess {String} requests.id 请求ID
     * @apiSuccess {String} requests.email 邮箱
     * @apiSuccess {String} requests.fullname 姓名
     * @apiSuccess {String} requests.message 附加说明
     * @apiSuccess {String} requests.status 当前状态
     * @apiSuccess {Number} requests.created_date 提交时间戳
     * @apiError (client) ForbiddenError 没有权限
     * @apiPermission admin
     * @apiVersion 1.0.0
     *
     * @return Response
     */
    @GET
    public Response list() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        // 构建请求列表
        JsonArrayBuilder reqArray = Json.createArrayBuilder();
        requestService.requestDao.findAll().forEach(req -> {
            reqArray.add(Json.createObjectBuilder()
                    .add("id", req.getId())
                    .add("email", req.getEmail())
                    .add("fullname", req.getFullname())
                    .add("message", req.getMessage() == null ? "" : req.getMessage())
                    .add("status", req.getStatus().toUpperCase())
                    .add("created_date", req.getCreatedDate().getTime()));
        });
        JsonObjectBuilder response = Json.createObjectBuilder().add("requests", reqArray);
        return Response.ok(response.build()).build();
    }

    /**
     * 管理员审核注册请求（接受或拒绝）。
     *
     * @api {put} /registration-request/:id 审核注册请求
     * @apiName PutRegistrationRequest
     * @apiGroup RegistrationRequest
     * @apiParam {String="accepted","rejected"} status 要设置的状态
     * @apiSuccess {String} status 状态 OK
     * @apiSuccess {String} [password] 若接受请求，则返回生成的初始密码
     * @apiError (client) ForbiddenError 没有权限
     * @apiError (client) ValidationError 参数验证失败
     * @apiError (client) AlreadyProcessed 请求已处理
     * @apiError (client) UnknownRequest 请求不存在
     * @apiVersion 1.0.0
     *
     * @param id 注册请求ID（路径参数）
     * @param body JSON形式的请求体，包含status字段
     * @return Response
     */
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response review(@PathParam("id") String id, String body) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        // 解析 JSON 请求体
        JsonObject json;
        try (JsonReader reader = Json.createReader(new StringReader(body))) {
            json = reader.readObject();
        }
        String status = json.containsKey("status") ? json.getString("status") : null;
        if (status == null || status.isEmpty()) {
            throw new ClientException("ValidationError", "缺少status参数");
        }
        String statusLower = status.toLowerCase();
        JsonObjectBuilder response = Json.createObjectBuilder().add("status", "ok");
        if ("accepted".equals(statusLower)) {
            // 接受请求，创建用户并获取初始密码
            String password = requestService.acceptRequest(id);
            // 可在此发送邮件通知访客，但此处通过界面反馈
            response.add("password", password);
        } else if ("rejected".equals(statusLower)) {
            // 拒绝请求
            requestService.rejectRequest(id);
        } else {
            throw new ClientException("ValidationError", "无效的status值");
        }
        return Response.ok(response.build()).build();
    }
}
