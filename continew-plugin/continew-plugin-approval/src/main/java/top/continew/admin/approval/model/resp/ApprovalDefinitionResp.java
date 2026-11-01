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
 * 审批定义响应信息
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Data
@Schema(description = "审批定义响应信息")
public class ApprovalDefinitionResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    private Long id;

    /**
     * 审批编码
     */
    @Schema(description = "审批编码", example = "LEAVE_APPROVAL")
    private String code;

    /**
     * 审批名称
     */
    @Schema(description = "审批名称", example = "请假审批")
    private String name;

    /**
     * 审批分类
     */
    @Schema(description = "审批分类", example = "人事管理")
    private String category;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "用于员工请假申请的审批流程")
    private String description;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private Integer status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-08-08 08:08:08", type = "string")
    private LocalDateTime createTime;
}
