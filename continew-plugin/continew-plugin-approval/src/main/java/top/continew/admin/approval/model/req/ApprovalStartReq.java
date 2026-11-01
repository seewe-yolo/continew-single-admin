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

import jakarta.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;

/**
 * 审批发起请求参数
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Data
@Schema(description = "审批发起请求参数")
public class ApprovalStartReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 审批定义编码
     */
    @Schema(description = "审批定义编码", example = "LEAVE_APPROVAL")
    @NotBlank(message = "审批定义编码不能为空")
    private String definitionCode;

    /**
     * 业务主键
     */
    @Schema(description = "业务主键", example = "123456")
    @NotBlank(message = "业务主键不能为空")
    @Length(max = 100, message = "业务主键长度不能超过 {max} 个字符")
    private String businessKey;

    /**
     * 业务类型
     */
    @Schema(description = "业务类型", example = "LEAVE")
    @NotBlank(message = "业务类型不能为空")
    @Length(max = 50, message = "业务类型长度不能超过 {max} 个字符")
    private String businessType;

    /**
     * 审批标题
     */
    @Schema(description = "审批标题", example = "张三的请假申请")
    @NotBlank(message = "审批标题不能为空")
    @Length(max = 200, message = "审批标题长度不能超过 {max} 个字符")
    private String title;

    /**
     * 表单数据（JSON格式）
     */
    @Schema(description = "表单数据")
    private String formData;
}
