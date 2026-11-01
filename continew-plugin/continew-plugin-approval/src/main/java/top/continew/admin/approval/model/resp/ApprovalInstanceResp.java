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

package top.continew.admin.approval.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审批实例响应信息
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Data
@Schema(description = "审批实例响应信息")
public class ApprovalInstanceResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    private Long id;

    private String definitionCode;
    private String businessKey;
    private Long startUser;
    private String currentNodeId;
    private LocalDateTime endTime;

    /**
     * 审批标题
     */
    @Schema(description = "审批标题", example = "张三的请假申请")
    private String title;

    /**
     * 业务类型
     */
    @Schema(description = "业务类型", example = "LEAVE")
    private String businessType;

    /**
     * 当前节点名称
     */
    @Schema(description = "当前节点名称", example = "部门主管审批")
    private String currentNodeName;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "PENDING")
    private String status;

    /**
     * 发起人
     */
    @Schema(description = "发起人", example = "张三")
    private String startUserName;

    /**
     * 发起时间
     */
    @Schema(description = "发起时间", example = "2023-08-08 08:08:08", type = "string")
    private LocalDateTime startTime;
}
