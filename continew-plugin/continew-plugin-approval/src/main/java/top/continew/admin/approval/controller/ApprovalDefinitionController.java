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

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.approval.model.query.ApprovalDefinitionQuery;
import top.continew.admin.approval.model.req.ApprovalDefinitionReq;
import top.continew.admin.approval.model.resp.ApprovalDefinitionDetailResp;
import top.continew.admin.approval.model.resp.ApprovalDefinitionResp;
import top.continew.admin.approval.service.ApprovalDefinitionService;
import top.continew.admin.common.base.controller.BaseController;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.enums.Api;

/**
 * 审批定义管理 API
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Tag(name = "审批定义管理 API")
@Validated
@RestController
@RequiredArgsConstructor
@CrudRequestMapping(value = "/approval/definition",
    api = {Api.PAGE, Api.LIST, Api.GET, Api.CREATE, Api.UPDATE, Api.BATCH_DELETE})
public class ApprovalDefinitionController extends
    BaseController<ApprovalDefinitionService, ApprovalDefinitionResp, ApprovalDefinitionDetailResp, ApprovalDefinitionQuery, ApprovalDefinitionReq> {
}
