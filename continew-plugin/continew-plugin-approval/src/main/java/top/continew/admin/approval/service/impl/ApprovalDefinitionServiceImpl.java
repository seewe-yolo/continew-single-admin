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

package top.continew.admin.approval.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.admin.approval.mapper.ApprovalDefinitionMapper;
import top.continew.admin.approval.model.entity.ApprovalDefinitionDO;
import top.continew.admin.approval.model.query.ApprovalDefinitionQuery;
import top.continew.admin.approval.model.req.ApprovalDefinitionReq;
import top.continew.admin.approval.model.resp.ApprovalDefinitionDetailResp;
import top.continew.admin.approval.model.resp.ApprovalDefinitionResp;
import top.continew.admin.approval.service.ApprovalDefinitionService;
import top.continew.admin.approval.service.ApprovalFlowValidator;
import top.continew.admin.common.base.service.BaseServiceImpl;
import top.continew.starter.core.util.validation.CheckUtils;

/**
 * 审批定义服务实现
 *
 * @author Charles7c
 * @since 5.0.0
 */
// @formatter:off
@Service
@RequiredArgsConstructor
public class ApprovalDefinitionServiceImpl extends
    BaseServiceImpl<ApprovalDefinitionMapper, ApprovalDefinitionDO, ApprovalDefinitionResp,
        ApprovalDefinitionDetailResp, ApprovalDefinitionQuery, ApprovalDefinitionReq>
    implements ApprovalDefinitionService {
    // @formatter:on

    private final ApprovalFlowValidator flowValidator;

    @Override
    public void beforeCreate(ApprovalDefinitionReq req) {
        flowValidator.validate(req.getFlowConfig());
        flowValidator.validateFormConfig(req.getFormConfig());
    }

    @Override
    public void beforeUpdate(ApprovalDefinitionReq req, Long id) {
        ApprovalDefinitionDO old = super.getById(id);
        CheckUtils.throwIfNotEqual(req.getCode(), old.getCode(), "审批编码不允许修改");
        flowValidator.validate(req.getFlowConfig());
        flowValidator.validateFormConfig(req.getFormConfig());
    }
}
