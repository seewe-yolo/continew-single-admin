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

package top.continew.admin.approval.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.continew.admin.approval.mapper.ApprovalHistoryMapper;
import top.continew.admin.approval.mapper.ApprovalInstanceMapper;
import top.continew.admin.approval.mapper.ApprovalTaskMapper;
import top.continew.admin.approval.model.dto.FlowConfigDTO;
import top.continew.admin.approval.model.entity.ApprovalHistoryDO;
import top.continew.admin.approval.model.entity.ApprovalInstanceDO;
import top.continew.admin.approval.model.entity.ApprovalTaskDO;
import top.continew.admin.approval.model.query.ApprovalInstanceQuery;
import top.continew.admin.approval.model.query.ApprovalTaskQuery;
import top.continew.admin.approval.model.resp.ApprovalDetailResp;
import top.continew.admin.approval.model.resp.ApprovalInstanceResp;
import top.continew.admin.approval.model.resp.ApprovalTaskResp;
import top.continew.admin.common.api.system.UserApi;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 审批查询，始终按发起人或任务参与人授权。
 *
 * @author seewis
 * @since 5.0.0
 */
@Service
@RequiredArgsConstructor
public class ApprovalQueryService {

    private final ApprovalInstanceMapper instanceMapper;
    private final ApprovalTaskMapper taskMapper;
    private final ApprovalHistoryMapper historyMapper;
    private final UserApi userApi;

    /**
     * 分页查询本人的申请。
     * @param query 过滤条件
     * @param pageQuery 分页参数
     * @return 申请列表
     */
    public PageResp<ApprovalInstanceResp> pageMine(ApprovalInstanceQuery query,
        PageQuery pageQuery) {
        requireUser();
        Page<ApprovalInstanceDO> page = instanceMapper.selectPage(new Page<>(pageQuery.getPage(),
            pageQuery.getSize()),
            new LambdaQueryWrapper<ApprovalInstanceDO>()
                .eq(ApprovalInstanceDO::getStartUser, UserContextHolder.getUserId())
                .like(StrUtil.isNotBlank(query.getTitle()), ApprovalInstanceDO::getTitle,
                    query.getTitle())
                .eq(StrUtil.isNotBlank(query.getStatus()), ApprovalInstanceDO::getStatus,
                    query.getStatus())
                .eq(StrUtil.isNotBlank(query.getBusinessType()),
                    ApprovalInstanceDO::getBusinessType,
                    query.getBusinessType())
                .orderByDesc(ApprovalInstanceDO::getStartTime)
                .orderByDesc(ApprovalInstanceDO::getId));
        PageResp<ApprovalInstanceResp> result = PageResp.build(page, ApprovalInstanceResp.class);
        for (int i = 0; i < page.getRecords().size(); i++) {
            ApprovalInstanceDO instance = page.getRecords().get(i);
            ApprovalInstanceResp row = result.getList().get(i);
            row.setStartUserName(UserContextHolder.getContext().getNickname());
            if (StrUtil.isNotBlank(instance.getFlowConfig())) {
                FlowConfigDTO config =
                    JSONUtil.toBean(instance.getFlowConfig(), FlowConfigDTO.class);
                config.getNodes().stream()
                    .filter(n -> Objects.equals(n.getId(), instance.getCurrentNodeId()))
                    .findFirst().ifPresent(n -> row.setCurrentNodeName(n.getName()));
            }
        }
        return result;
    }

    /**
     * 分页查询分配给本人的待办或已处理任务。
     * @param query 过滤条件
     * @param pageQuery 分页参数
     * @param completed 是否已处理
     * @return 任务列表
     */
    public PageResp<ApprovalTaskResp> pageTasks(ApprovalTaskQuery query, PageQuery pageQuery,
        boolean completed) {
        requireUser();
        return PageResp.build(taskMapper.selectMyPage(new Page<>(pageQuery.getPage(),
            pageQuery.getSize()), UserContextHolder.getUserId(),
            query, completed));
    }

    /**
     * 查看本人发起或曾被分配任务的审批，不能凭全局权限查看其他人的申请。
     * @param id 审批实例 ID
     * @return 详情和历史
     */
    @Transactional(readOnly = true)
    public ApprovalDetailResp detail(Long id) {
        requireUser();
        ApprovalInstanceDO instance = instanceMapper.selectById(id);
        CheckUtils.throwIfNull(instance, "审批实例不存在");
        if (!Objects.equals(instance.getStartUser(), UserContextHolder.getUserId())) {
            Long count = taskMapper.selectCount(new LambdaQueryWrapper<ApprovalTaskDO>()
                .eq(ApprovalTaskDO::getInstanceId, id)
                .eq(ApprovalTaskDO::getAssignee, UserContextHolder.getUserId()));
            CheckUtils.throwIf(count == 0, "无权查看此审批");
        }
        ApprovalDetailResp result = new ApprovalDetailResp();
        result.setInstance(instance);
        result.setTasks(taskMapper.selectList(new LambdaQueryWrapper<ApprovalTaskDO>()
            .eq(ApprovalTaskDO::getInstanceId, id)
            .orderByAsc(ApprovalTaskDO::getCreateTime).orderByAsc(ApprovalTaskDO::getId)));
        result.setHistory(historyMapper.selectList(new LambdaQueryWrapper<ApprovalHistoryDO>()
            .eq(ApprovalHistoryDO::getInstanceId, id)
            .orderByAsc(ApprovalHistoryDO::getCreateTime).orderByAsc(ApprovalHistoryDO::getId)));
        Map<Long, String> names = new HashMap<>();
        names.put(instance.getStartUser(), "");
        result.getTasks().forEach(t -> names.put(t.getAssignee(), ""));
        result.getHistory().forEach(h -> names.put(h.getOperator(), ""));
        names
            .replaceAll((userId, ignored) -> StrUtil.blankToDefault(userApi.getNicknameById(userId),
                String.valueOf(userId)));
        result.setUserNames(names);
        return result;
    }

    private void requireUser() {
        CheckUtils.throwIfNull(UserContextHolder.getUserId(), "请先登录");
    }
}
