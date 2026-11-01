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

package top.continew.admin.approval.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.continew.admin.approval.enums.ApprovalActionEnum;
import top.continew.admin.approval.enums.ApprovalStatusEnum;
import top.continew.admin.approval.enums.ApproveModeEnum;
import top.continew.admin.approval.mapper.ApprovalDefinitionMapper;
import top.continew.admin.approval.mapper.ApprovalHistoryMapper;
import top.continew.admin.approval.mapper.ApprovalInstanceMapper;
import top.continew.admin.approval.mapper.ApprovalTaskMapper;
import top.continew.admin.approval.model.dto.FlowConfigDTO;
import top.continew.admin.approval.model.dto.FlowNodeDTO;
import top.continew.admin.approval.model.dto.FormConfigDTO;
import top.continew.admin.approval.model.entity.ApprovalDefinitionDO;
import top.continew.admin.approval.model.entity.ApprovalHistoryDO;
import top.continew.admin.approval.model.entity.ApprovalInstanceDO;
import top.continew.admin.approval.model.entity.ApprovalTaskDO;
import top.continew.admin.approval.model.req.ApprovalCancelReq;
import top.continew.admin.approval.model.req.ApprovalOpinionReq;
import top.continew.admin.approval.model.req.ApprovalStartReq;
import top.continew.admin.approval.service.ApprovalEngineService;
import top.continew.admin.approval.service.ApprovalFlowValidator;
import top.continew.admin.common.api.system.UserApi;
import top.continew.admin.common.constant.GlobalConstants;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.starter.core.util.validation.CheckUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 审批引擎服务实现
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Service
@RequiredArgsConstructor
public class ApprovalEngineServiceImpl implements ApprovalEngineService {

    private final ApprovalDefinitionMapper definitionMapper;
    private final ApprovalInstanceMapper instanceMapper;
    private final ApprovalTaskMapper taskMapper;
    private final ApprovalHistoryMapper historyMapper;
    private final ApprovalFlowValidator flowValidator;
    private final UserApi userApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long startApproval(ApprovalStartReq req) {
        requireContext();
        // 1. 查询审批定义
        ApprovalDefinitionDO definition = definitionMapper.selectOne(
            new LambdaQueryWrapper<ApprovalDefinitionDO>()
                .eq(ApprovalDefinitionDO::getCode, req.getDefinitionCode())
                .eq(ApprovalDefinitionDO::getStatus, ApprovalDefinitionDO.StatusEnum.ENABLED));
        CheckUtils.throwIfNull(definition, "审批定义不存在或已禁用");

        // 2. 解析流程配置
        final FlowConfigDTO flowConfig = flowValidator.validate(definition.getFlowConfig());
        String formConfigJson = StrUtil.blankToDefault(definition.getFormConfig(),
            flowValidator.defaultFormConfig());
        FormConfigDTO formConfig = flowValidator.validateFormConfig(formConfigJson);
        flowValidator.validateFormData(formConfig, req.getFormData());

        // 3. 创建审批实例
        ApprovalInstanceDO instance = new ApprovalInstanceDO();
        instance.setDefinitionCode(req.getDefinitionCode());
        instance.setBusinessKey(req.getBusinessKey());
        instance.setBusinessType(req.getBusinessType());
        instance.setTitle(req.getTitle());
        instance.setFormData(req.getFormData());
        instance.setFormConfig(formConfigJson);
        instance.setFlowConfig(definition.getFlowConfig());
        instance.setStatus(ApprovalStatusEnum.PENDING);
        instance.setStartUser(UserContextHolder.getUserId());
        instance.setStartTime(LocalDateTime.now(GlobalConstants.DEFAULT_ZONE_ID));

        FlowNodeDTO firstNode = flowConfig.getNodes().get(0);
        instance.setCurrentNodeId(firstNode.getId());

        instanceMapper.insert(instance);

        // 4. 创建第一个节点的审批任务
        this.createTasksForNode(instance, firstNode);

        // 5. 记录启动历史
        this.recordHistory(instance.getId(), null, null, null,
            UserContextHolder.getUserId(), ApprovalActionEnum.START, "发起审批");

        return instance.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long taskId, ApprovalOpinionReq req) {
        requireContext();
        ApprovalTaskDO initialTask = taskMapper.selectById(taskId);
        CheckUtils.throwIfNull(initialTask, "审批任务不存在");
        ApprovalInstanceDO lockedInstance = lockInstance(initialTask.getInstanceId());

        // 1. 查询任务
        ApprovalTaskDO task = taskMapper.selectOne(new LambdaQueryWrapper<ApprovalTaskDO>()
            .eq(ApprovalTaskDO::getId, taskId).last("FOR UPDATE"));
        CheckUtils.throwIfNull(task, "审批任务不存在");
        CheckUtils.throwIfNotEqual(task.getAssignee(), UserContextHolder.getUserId(), "无权限审批此任务");
        CheckUtils.throwIfNotEqual(task.getStatus(), ApprovalStatusEnum.PENDING, "任务状态不正确");

        // 2. 查询审批实例
        ApprovalInstanceDO instance = lockedInstance;
        CheckUtils.throwIfNull(instance, "审批实例不存在");
        checkCurrentTask(task, instance);

        // 3. 更新任务状态
        task.setStatus(ApprovalStatusEnum.APPROVED);
        task.setOpinion(req.getOpinion());
        task.setCompleteTime(LocalDateTime.now(GlobalConstants.DEFAULT_ZONE_ID));
        taskMapper.updateById(task);

        // 4. 记录历史
        this.recordHistory(instance.getId(), taskId, task.getNodeId(), task.getNodeName(),
            UserContextHolder.getUserId(), ApprovalActionEnum.APPROVE, req.getOpinion());

        // 5. 检查当前节点是否完成
        if (this.checkNodeCompleted(instance, task.getNodeId())) {
            this.cancelOtherPendingTasks(instance.getId(), task.getNodeId(), taskId);
            // 6. 流转到下一节点或结束流程
            this.flowToNextNodeOrComplete(instance);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long taskId, ApprovalOpinionReq req) {
        requireContext();
        ApprovalTaskDO initialTask = taskMapper.selectById(taskId);
        CheckUtils.throwIfNull(initialTask, "审批任务不存在");
        ApprovalInstanceDO lockedInstance = lockInstance(initialTask.getInstanceId());

        // 1. 查询任务
        ApprovalTaskDO task = taskMapper.selectOne(new LambdaQueryWrapper<ApprovalTaskDO>()
            .eq(ApprovalTaskDO::getId, taskId).last("FOR UPDATE"));
        CheckUtils.throwIfNull(task, "审批任务不存在");
        CheckUtils.throwIfNotEqual(task.getAssignee(), UserContextHolder.getUserId(), "无权限审批此任务");
        CheckUtils.throwIfNotEqual(task.getStatus(), ApprovalStatusEnum.PENDING, "任务状态不正确");

        // 2. 查询审批实例
        ApprovalInstanceDO instance = lockedInstance;
        CheckUtils.throwIfNull(instance, "审批实例不存在");
        checkCurrentTask(task, instance);

        // 3. 更新任务状态
        task.setStatus(ApprovalStatusEnum.REJECTED);
        task.setOpinion(req.getOpinion());
        task.setCompleteTime(LocalDateTime.now(GlobalConstants.DEFAULT_ZONE_ID));
        taskMapper.updateById(task);

        // 4. 取消当前节点的其他待办任务
        this.cancelOtherPendingTasks(instance.getId(), task.getNodeId(), taskId);

        // 5. 结束流程
        instance.setStatus(ApprovalStatusEnum.REJECTED);
        instance.setResult("REJECTED");
        instance.setEndTime(LocalDateTime.now(GlobalConstants.DEFAULT_ZONE_ID));
        instanceMapper.updateById(instance);

        // 6. 记录历史
        this.recordHistory(instance.getId(), taskId, task.getNodeId(), task.getNodeName(),
            UserContextHolder.getUserId(), ApprovalActionEnum.REJECT, req.getOpinion());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long instanceId, ApprovalCancelReq req) {
        requireContext();
        // 1. 查询审批实例
        ApprovalInstanceDO instance = lockInstance(instanceId);
        CheckUtils.throwIfNull(instance, "审批实例不存在");
        CheckUtils.throwIfNotEqual(instance.getStartUser(), UserContextHolder.getUserId(),
            "只有发起人可以撤销");
        CheckUtils.throwIfNotEqual(instance.getStatus(), ApprovalStatusEnum.PENDING, "当前状态不允许撤销");

        // 2. 取消所有待办任务
        taskMapper.update(null, new LambdaUpdateWrapper<ApprovalTaskDO>()
            .eq(ApprovalTaskDO::getInstanceId, instanceId)
            .eq(ApprovalTaskDO::getStatus, ApprovalStatusEnum.PENDING)
            .set(ApprovalTaskDO::getStatus, ApprovalStatusEnum.CANCELLED)
            .set(ApprovalTaskDO::getCompleteTime,
                LocalDateTime.now(GlobalConstants.DEFAULT_ZONE_ID)));

        // 3. 更新实例状态
        instance.setStatus(ApprovalStatusEnum.CANCELLED);
        instance.setResult("CANCELLED");
        instance.setEndTime(LocalDateTime.now(GlobalConstants.DEFAULT_ZONE_ID));
        instanceMapper.updateById(instance);

        // 4. 记录历史
        this.recordHistory(instanceId, null, null, null,
            UserContextHolder.getUserId(), ApprovalActionEnum.CANCEL, req.getReason());
    }

    /**
     * 为节点创建审批任务
     *
     * @param instance 审批实例
     * @param node     流程节点
     */
    private void createTasksForNode(ApprovalInstanceDO instance, FlowNodeDTO node) {
        CheckUtils.throwIfEmpty(node.getApprovers(), "节点 [{}] 未配置审批人", node.getName());

        for (Long approverId : node.getApprovers()) {
            ApprovalTaskDO task = new ApprovalTaskDO();
            CheckUtils.throwIf(!userApi.isAvailable(approverId), "审批人不存在或已禁用");
            task.setInstanceId(instance.getId());
            task.setNodeId(node.getId());
            task.setNodeName(node.getName());
            task.setAssignee(approverId);
            task.setStatus(ApprovalStatusEnum.PENDING);
            task.setCreateTime(LocalDateTime.now(GlobalConstants.DEFAULT_ZONE_ID));
            taskMapper.insert(task);
        }
    }

    /**
     * 检查节点是否完成
     *
     * @param instance 审批实例
     * @param nodeId   节点ID
     * @return true：已完成；false：未完成
     */
    private boolean checkNodeCompleted(ApprovalInstanceDO instance, String nodeId) {
        // 查询当前节点的所有任务
        List<ApprovalTaskDO> nodeTasks = taskMapper.selectList(
            new LambdaQueryWrapper<ApprovalTaskDO>()
                .eq(ApprovalTaskDO::getInstanceId, instance.getId())
                .eq(ApprovalTaskDO::getNodeId, nodeId).last("FOR UPDATE"));

        if (nodeTasks.isEmpty()) {
            return false;
        }

        CheckUtils.throwIfBlank(instance.getFlowConfig(), "流程快照缺失，请联系管理员处理");
        // 获取节点配置
        FlowConfigDTO flowConfig = JSONUtil.toBean(instance.getFlowConfig(), FlowConfigDTO.class);
        FlowNodeDTO node = flowConfig.getNodes().stream()
            .filter(n -> n.getId().equals(nodeId))
            .findFirst()
            .orElse(null);

        CheckUtils.throwIfNull(node, "流程快照与当前节点不一致，请联系管理员处理");

        // 判断是否完成
        if (ApproveModeEnum.OR.equals(node.getApproveMode())) {
            // 或签：任一通过即可
            return nodeTasks.stream()
                .anyMatch(t -> ApprovalStatusEnum.APPROVED.equals(t.getStatus()));
        } else {
            // 会签：全部通过
            return nodeTasks.stream()
                .allMatch(t -> ApprovalStatusEnum.APPROVED.equals(t.getStatus()));
        }
    }

    /**
     * 流转到下一节点或完成流程
     *
     * @param instance 审批实例
     */
    private void flowToNextNodeOrComplete(ApprovalInstanceDO instance) {
        // 查询审批定义
        FlowConfigDTO flowConfig = JSONUtil.toBean(instance.getFlowConfig(), FlowConfigDTO.class);

        // 获取下一个节点
        FlowNodeDTO nextNode = this.getNextNode(flowConfig, instance.getCurrentNodeId());

        if (nextNode != null) {
            // 流转到下一节点
            instance.setCurrentNodeId(nextNode.getId());
            instanceMapper.updateById(instance);

            // 创建下一节点的任务
            this.createTasksForNode(instance, nextNode);
        } else {
            // 流程结束
            instance.setStatus(ApprovalStatusEnum.APPROVED);
            instance.setResult("APPROVED");
            instance.setEndTime(LocalDateTime.now(GlobalConstants.DEFAULT_ZONE_ID));
            instance.setCurrentNodeId(null);
            instanceMapper.update(null, new LambdaUpdateWrapper<ApprovalInstanceDO>()
                .eq(ApprovalInstanceDO::getId, instance.getId())
                .set(ApprovalInstanceDO::getCurrentNodeId, null));
            instanceMapper.updateById(instance);
        }
    }

    /**
     * 获取下一个节点
     *
     * @param flowConfig  流程配置
     * @param currentNodeId 当前节点ID
     * @return 下一个节点，如果没有则返回 null
     */
    private FlowNodeDTO getNextNode(FlowConfigDTO flowConfig, String currentNodeId) {
        List<FlowNodeDTO> nodes = flowConfig.getNodes();
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getId().equals(currentNodeId)) {
                if (i + 1 < nodes.size()) {
                    return nodes.get(i + 1);
                }
                break;
            }
        }
        return null;
    }

    /**
     * 取消其他待办任务
     *
     * @param instanceId 审批实例ID
     * @param nodeId     节点ID
     * @param excludeTaskId 排除的任务ID
     */
    private void cancelOtherPendingTasks(Long instanceId, String nodeId, Long excludeTaskId) {
        taskMapper.update(null, new LambdaUpdateWrapper<ApprovalTaskDO>()
            .eq(ApprovalTaskDO::getInstanceId, instanceId)
            .eq(ApprovalTaskDO::getNodeId, nodeId)
            .ne(ApprovalTaskDO::getId, excludeTaskId)
            .eq(ApprovalTaskDO::getStatus, ApprovalStatusEnum.PENDING)
            .set(ApprovalTaskDO::getStatus, ApprovalStatusEnum.CANCELLED)
            .set(ApprovalTaskDO::getCompleteTime,
                LocalDateTime.now(GlobalConstants.DEFAULT_ZONE_ID)));
    }

    /**
     * 记录审批历史
     *
     * @param instanceId 审批实例ID
     * @param taskId     任务ID
     * @param nodeId     节点ID
     * @param nodeName   节点名称
     * @param operator   操作人
     * @param action     操作
     * @param opinion    意见
     */
    private void recordHistory(Long instanceId, Long taskId, String nodeId, String nodeName,
        Long operator, ApprovalActionEnum action, String opinion) {
        ApprovalHistoryDO history = new ApprovalHistoryDO();
        history.setInstanceId(instanceId);
        history.setTaskId(taskId);
        history.setNodeId(nodeId);
        history.setNodeName(nodeName);
        history.setOperator(operator);
        history.setAction(action);
        history.setOpinion(opinion);
        history.setCreateTime(LocalDateTime.now(GlobalConstants.DEFAULT_ZONE_ID));
        historyMapper.insert(history);
    }

    private void requireContext() {
        CheckUtils.throwIfNull(UserContextHolder.getUserId(), "请先登录");
    }

    private ApprovalInstanceDO lockInstance(Long id) {
        ApprovalInstanceDO instance =
            instanceMapper.selectOne(new LambdaQueryWrapper<ApprovalInstanceDO>()
                .eq(ApprovalInstanceDO::getId, id)
                .last("FOR UPDATE"));
        CheckUtils.throwIfNull(instance, "审批实例不存在");
        CheckUtils.throwIfNotEqual(instance.getStatus(), ApprovalStatusEnum.PENDING, "审批已结束，请刷新列表");
        return instance;
    }

    private void checkCurrentTask(ApprovalTaskDO task, ApprovalInstanceDO instance) {
        CheckUtils.throwIfBlank(instance.getFlowConfig(), "流程快照缺失，请联系管理员处理");
        FlowConfigDTO config = JSONUtil.toBean(instance.getFlowConfig(), FlowConfigDTO.class);
        CheckUtils.throwIfEmpty(config.getNodes(), "流程快照缺失，请联系管理员处理");
        CheckUtils.throwIf(
            config.getNodes().stream()
                .noneMatch(n -> Objects.equals(n.getId(), instance.getCurrentNodeId())),
            "流程快照与当前节点不一致，请联系管理员处理");
        CheckUtils.throwIfNotEqual(task.getNodeId(), instance.getCurrentNodeId(), "任务节点已变更，请刷新列表");
    }
}
