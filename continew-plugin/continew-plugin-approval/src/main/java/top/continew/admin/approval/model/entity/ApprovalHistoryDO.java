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

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.continew.admin.approval.enums.ApprovalActionEnum;
import top.continew.starter.extension.crud.model.entity.BaseIdDO;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 审批历史实体
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Data
@TableName(value = "approval_history", autoResultMap = true)
public class ApprovalHistoryDO extends BaseIdDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 审批实例ID
     */
    private Long instanceId;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 节点ID
     */
    private String nodeId;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 操作人
     */
    private Long operator;

    /**
     * 操作
     */
    private ApprovalActionEnum action;

    /**
     * 意见
     */
    private String opinion;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
