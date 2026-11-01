/*
 * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.continew.admin.approval.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.approval.model.query.ApprovalInstanceQuery;
import top.continew.admin.approval.model.req.ApprovalCancelReq;
import top.continew.admin.approval.model.req.ApprovalStartReq;
import top.continew.admin.approval.model.resp.ApprovalDetailResp;
import top.continew.admin.approval.model.resp.ApprovalInstanceResp;
import top.continew.admin.approval.service.ApprovalEngineService;
import top.continew.admin.approval.service.ApprovalQueryService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;
import top.continew.starter.web.model.R;

/**
 * 审批实例管理 API
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Tag(name = "审批实例管理 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/approval/instance")
public class ApprovalInstanceController {

    private final ApprovalEngineService approvalEngineService;
    private final ApprovalQueryService approvalQueryService;

    @Operation(summary = "发起审批", description = "发起审批")
    @SaCheckPermission("approval:instance:start")
    @PostMapping("/start")
    public R<Long> start(@Validated @RequestBody ApprovalStartReq req) {
        return R.ok(approvalEngineService.startApproval(req));
    }

    @Operation(summary = "撤销审批", description = "撤销审批")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("approval:instance:cancel")
    @PostMapping("/cancel/{id}")
    public R<Void> cancel(@PathVariable Long id,
        @Validated @RequestBody ApprovalCancelReq req) {
        approvalEngineService.cancel(id, req);
        return R.ok();
    }

    @Operation(summary = "我的申请")
    @SaCheckPermission("approval:instance:list")
    @GetMapping
    public PageResp<ApprovalInstanceResp> page(@Validated ApprovalInstanceQuery query,
        @Validated PageQuery pageQuery) {
        return approvalQueryService.pageMine(query, pageQuery);
    }

    @Operation(summary = "参与的审批详情及历史")
    @SaCheckPermission("approval:instance:get")
    @GetMapping("/{id}")
    public ApprovalDetailResp detail(@PathVariable Long id) {
        return approvalQueryService.detail(id);
    }
}
