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

package top.continew.admin.approval.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.continew.admin.approval.model.entity.ApprovalTaskDO;
import top.continew.admin.approval.model.query.ApprovalTaskQuery;
import top.continew.admin.approval.model.resp.ApprovalTaskResp;
import top.continew.starter.data.mapper.BaseMapper;

/**
 * 审批任务 Mapper
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Mapper
public interface ApprovalTaskMapper extends BaseMapper<ApprovalTaskDO> {

    /**
     * 查询分配给指定用户的任务。
     * @param page 分页
     * @param userId 当前用户
     * @param query 条件
     * @param completed 是否已处理
     * @return 任务页
     */
    IPage<ApprovalTaskResp> selectMyPage(@Param("page") Page<ApprovalTaskResp> page,
        @Param("userId") Long userId,
        @Param("query") ApprovalTaskQuery query, @Param("completed") boolean completed);
}
