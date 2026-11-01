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

package top.continew.admin.approval.model.dto;

import lombok.Data;
import top.continew.admin.approval.enums.ApproveModeEnum;
import top.continew.admin.approval.enums.ApproverTypeEnum;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 流程节点配置
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Data
public class FlowNodeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 节点ID
     */
    private String id;

    /**
     * 节点名称
     */
    private String name;

    /**
     * 节点类型（APPROVAL：审批节点）
     */
    private String type;

    /**
     * 审批人类型
     */
    private ApproverTypeEnum approverType;

    /**
     * 审批人列表（用户ID、角色ID或部门ID）
     */
    private List<Long> approvers;

    /**
     * 审批模式
     */
    private ApproveModeEnum approveMode;
}
