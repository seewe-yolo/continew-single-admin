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

package top.continew.admin.approval.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.continew.admin.approval.config.ApprovalJsonTypeHandler;
import top.continew.admin.common.base.model.entity.BaseDO;
import top.continew.starter.core.enums.BaseEnum;

import java.io.Serial;

/**
 * 审批定义实体
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Data
@TableName(value = "approval_definition", autoResultMap = true)
public class ApprovalDefinitionDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 审批编码
     */
    private String code;

    /**
     * 审批名称
     */
    private String name;

    /**
     * 审批分类
     */
    private String category;

    /**
     * 流程配置（JSON格式）
     */
    @TableField(typeHandler = ApprovalJsonTypeHandler.class)
    private String flowConfig;

    /**
     * 申请表单配置（JSON格式）。
     */
    @TableField(typeHandler = ApprovalJsonTypeHandler.class)
    private String formConfig;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态
     */
    private StatusEnum status;

    /**
     * 状态枚举
     */
    @lombok.Getter
    @lombok.RequiredArgsConstructor
    public enum StatusEnum implements BaseEnum<Integer> {

        /**
         * 禁用
         */
        DISABLED(0, "禁用"),

        /**
         * 启用
         */
        ENABLED(1, "启用");

        private final Integer value;
        private final String description;
    }
}
