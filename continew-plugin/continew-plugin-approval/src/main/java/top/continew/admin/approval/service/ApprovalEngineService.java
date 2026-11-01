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

package top.continew.admin.approval.service;

import top.continew.admin.approval.model.req.ApprovalCancelReq;
import top.continew.admin.approval.model.req.ApprovalOpinionReq;
import top.continew.admin.approval.model.req.ApprovalStartReq;

/**
 * 审批引擎服务
 *
 * @author Charles7c
 * @since 5.0.0
 */
public interface ApprovalEngineService {

    /**
     * 启动审批流程
     *
     * @param req 审批发起请求参数
     * @return 审批实例ID
     */
    Long startApproval(ApprovalStartReq req);

    /**
     * 审批同意
     *
     * @param taskId  任务ID
     * @param req     审批意见请求参数
     */
    void approve(Long taskId, ApprovalOpinionReq req);

    /**
     * 审批拒绝
     *
     * @param taskId  任务ID
     * @param req     审批意见请求参数
     */
    void reject(Long taskId, ApprovalOpinionReq req);

    /**
     * 撤销审批
     *
     * @param instanceId 审批实例ID
     * @param req        撤销请求参数
     */
    void cancel(Long instanceId, ApprovalCancelReq req);
}
