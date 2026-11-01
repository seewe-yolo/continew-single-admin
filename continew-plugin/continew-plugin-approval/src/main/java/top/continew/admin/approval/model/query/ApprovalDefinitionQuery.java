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

package top.continew.admin.approval.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.continew.starter.data.annotation.Query;
import top.continew.starter.data.enums.QueryType;

import java.io.Serial;
import java.io.Serializable;

/**
 * 审批定义查询参数
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Data
@Schema(description = "审批定义查询参数")
public class ApprovalDefinitionQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 审批名称
     */
    @Schema(description = "审批名称", example = "请假审批")
    @Query(type = QueryType.LIKE)
    private String name;

    /**
     * 审批分类
     */
    @Schema(description = "审批分类", example = "人事管理")
    @Query
    private String category;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    @Query
    private Integer status;
}
