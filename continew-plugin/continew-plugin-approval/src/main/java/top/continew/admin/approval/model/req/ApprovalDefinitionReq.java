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

package top.continew.admin.approval.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;

/**
 * 审批定义创建或修改请求参数
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Data
@Schema(description = "审批定义创建或修改请求参数")
public class ApprovalDefinitionReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 审批编码
     */
    @Schema(description = "审批编码", example = "LEAVE_APPROVAL")
    @NotBlank(message = "审批编码不能为空")
    @Length(max = 50, message = "审批编码长度不能超过 {max} 个字符")
    private String code;

    /**
     * 审批名称
     */
    @Schema(description = "审批名称", example = "请假审批")
    @NotBlank(message = "审批名称不能为空")
    @Length(max = 100, message = "审批名称长度不能超过 {max} 个字符")
    private String name;

    /**
     * 审批分类
     */
    @Schema(description = "审批分类", example = "人事管理")
    @Length(max = 50, message = "审批分类长度不能超过 {max} 个字符")
    private String category;

    /**
     * 流程配置（JSON格式）
     */
    @Schema(description = "流程配置")
    @NotBlank(message = "流程配置不能为空")
    private String flowConfig;

    /**
     * 申请表单配置（JSON格式）。
     */
    @Schema(description = "申请表单配置")
    private String formConfig;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "用于员工请假申请的审批流程")
    @Length(max = 500, message = "描述长度不能超过 {max} 个字符")
    private String description;

    /**
     * 状态
     */
    @Schema(description = "状态（1启用 0禁用）", example = "1")
    @NotNull
    @Min(0)
    @Max(1)
    private Integer status;
}
