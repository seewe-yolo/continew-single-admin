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
 * 审批任务响应信息
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Data
@Schema(description = "审批任务响应信息")
public class ApprovalTaskResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    private Long id;

    private Long assignee;
    private Long startUser;

    /**
     * 审批实例ID
     */
    @Schema(description = "审批实例ID", example = "1")
    private Long instanceId;

    /**
     * 审批标题
     */
    @Schema(description = "审批标题", example = "张三的请假申请")
    private String title;

    /**
     * 节点名称
     */
    @Schema(description = "节点名称", example = "部门主管审批")
    private String nodeName;

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
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-08-08 08:08:08", type = "string")
    private LocalDateTime createTime;

    /**
     * 完成时间
     */
    @Schema(description = "完成时间", example = "2023-08-08 09:08:08", type = "string")
    private LocalDateTime completeTime;
}
