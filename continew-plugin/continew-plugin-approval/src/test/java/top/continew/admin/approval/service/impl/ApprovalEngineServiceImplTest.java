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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.springframework.context.support.GenericApplicationContext;
import top.continew.admin.approval.enums.ApprovalStatusEnum;
import top.continew.admin.approval.mapper.ApprovalDefinitionMapper;
import top.continew.admin.approval.mapper.ApprovalHistoryMapper;
import top.continew.admin.approval.mapper.ApprovalInstanceMapper;
import top.continew.admin.approval.mapper.ApprovalTaskMapper;
import top.continew.admin.approval.model.dto.FormConfigDTO;
import top.continew.admin.approval.model.entity.ApprovalInstanceDO;
import top.continew.admin.approval.model.entity.ApprovalTaskDO;
import top.continew.admin.approval.model.req.ApprovalCancelReq;
import top.continew.admin.approval.model.req.ApprovalOpinionReq;
import top.continew.admin.approval.service.ApprovalFlowValidator;
import top.continew.admin.approval.service.ApprovalQueryService;
import top.continew.admin.common.api.system.UserApi;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.starter.core.exception.BusinessException;

import jakarta.validation.Validator;
import java.util.List;

/**
 * 审批参与人权限与会签、或签回归检查，不连接数据库。
 *
 * @author seewis
 * @since 5.0.0
 */
class ApprovalEngineServiceImplTest {

    private ApprovalInstanceMapper instanceMapper;
    private ApprovalTaskMapper taskMapper;
    private ApprovalHistoryMapper historyMapper;
    private ApprovalEngineServiceImpl service;
    private ApprovalInstanceDO instance;
    private ApprovalTaskDO task;
    private UserApi userApi;

    @BeforeAll
    static void setUpApplicationContext() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("validator", Validator.class, () -> mock(Validator.class));
        RedissonClient redissonClient = mock(RedissonClient.class);
        when(redissonClient.getKeys()).thenReturn(mock(RKeys.class));
        context.getBeanFactory().registerSingleton("redissonClient", redissonClient);
        context.refresh();
        new SpringUtil().setApplicationContext(context);
        MapperBuilderAssistant assistant =
            new MapperBuilderAssistant(new MybatisConfiguration(), "test");
        assistant.setCurrentNamespace("approval-test");
        TableInfoHelper.initTableInfo(assistant, ApprovalInstanceDO.class);
        TableInfoHelper.initTableInfo(assistant, ApprovalTaskDO.class);
    }

    @BeforeEach
    void setUp() {
        instanceMapper = mock(ApprovalInstanceMapper.class);
        taskMapper = mock(ApprovalTaskMapper.class);
        historyMapper = mock(ApprovalHistoryMapper.class);
        userApi = mock(UserApi.class);
        service =
            new ApprovalEngineServiceImpl(mock(ApprovalDefinitionMapper.class), instanceMapper,
                taskMapper, historyMapper, mock(ApprovalFlowValidator.class), userApi);
        UserContext context = new UserContext();
        context.setId(7L);
        UserContextHolder.setContext(context);
        instance = new ApprovalInstanceDO();
        instance.setId(1L);
        instance.setStartUser(7L);
        instance.setCurrentNodeId("review");
        instance.setStatus(ApprovalStatusEnum.PENDING);
        instance.setFlowConfig(
            "{\"nodes\":[{\"id\":\"review\",\"name\":\"Review\",\"approveMode\":\"AND\"}]}");
        task = new ApprovalTaskDO();
        task.setId(2L);
        task.setInstanceId(1L);
        task.setNodeId("review");
        task.setAssignee(7L);
        task.setStatus(ApprovalStatusEnum.PENDING);
        when(instanceMapper.selectOne(any())).thenReturn(instance);
        when(taskMapper.selectById(2L)).thenReturn(task);
        when(taskMapper.selectOne(any())).thenReturn(task);
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clearContext();
    }

    @Test
    void shouldRejectCancellationByAnotherUser() {
        instance.setStartUser(8L);
        assertThrows(BusinessException.class, () -> service.cancel(1L, new ApprovalCancelReq()));
        verifyNoInteractions(taskMapper, historyMapper);
    }

    @Test
    void shouldRejectActionsAfterInstanceEnds() {
        instance.setStatus(ApprovalStatusEnum.CANCELLED);
        assertThrows(BusinessException.class, () -> service.approve(2L, new ApprovalOpinionReq()));
        verifyNoInteractions(historyMapper);
    }

    @Test
    void shouldRejectStaleNodeTask() {
        instance.setCurrentNodeId("next");
        assertThrows(BusinessException.class, () -> service.approve(2L, new ApprovalOpinionReq()));
        verifyNoInteractions(historyMapper);
    }

    @Test
    void shouldRejectTaskAssignedToAnotherUser() {
        task.setAssignee(9L);
        assertThrows(BusinessException.class, () -> service.approve(2L, new ApprovalOpinionReq()));
        verifyNoInteractions(historyMapper);
    }

    @Test
    void shouldNotCompleteCountersignWithCancelledTask() {
        ApprovalTaskDO old = new ApprovalTaskDO();
        old.setStatus(ApprovalStatusEnum.CANCELLED);
        when(taskMapper.selectList(any())).thenReturn(List.of(task, old));
        service.approve(2L, new ApprovalOpinionReq());
        assertEquals(ApprovalStatusEnum.PENDING, instance.getStatus());
        assertEquals(ApprovalStatusEnum.APPROVED, task.getStatus());
    }

    @Test
    void shouldWaitForOtherCountersigners() {
        ApprovalTaskDO other = new ApprovalTaskDO();
        other.setStatus(ApprovalStatusEnum.PENDING);
        when(taskMapper.selectList(any())).thenReturn(List.of(task, other));
        service.approve(2L, new ApprovalOpinionReq());
        assertEquals(ApprovalStatusEnum.PENDING, instance.getStatus());
    }

    @Test
    void shouldCompleteOrSignAndCancelRemainingTasks() {
        instance.setFlowConfig(instance.getFlowConfig().replace("AND", "OR"));
        ApprovalTaskDO other = new ApprovalTaskDO();
        other.setStatus(ApprovalStatusEnum.PENDING);
        when(taskMapper.selectList(any())).thenReturn(List.of(task, other));
        service.approve(2L, new ApprovalOpinionReq());
        assertEquals(ApprovalStatusEnum.APPROVED, instance.getStatus());
        verify(taskMapper).update(any(), any());
    }

    @Test
    void shouldDenyUnrelatedDetailEvenWithRoutePermission() {
        instance.setStartUser(8L);
        when(instanceMapper.selectById(1L)).thenReturn(instance);
        when(taskMapper.selectCount(any())).thenReturn(0L);
        ApprovalQueryService query =
            new ApprovalQueryService(instanceMapper, taskMapper, historyMapper, userApi);
        assertThrows(BusinessException.class, () -> query.detail(1L));
        verifyNoInteractions(historyMapper, userApi);
    }

    @Test
    void shouldRejectUnsupportedApproverType() {
        ApprovalFlowValidator validator = new ApprovalFlowValidator(userApi);
        String config = "{\"nodes\":[{\"id\":\"one\",\"name\":\"Review\",\"type\":\"APPROVAL\","
            + "\"approverType\":\"ROLE\",\"approveMode\":\"AND\",\"approvers\":[7]}]}";
        assertThrows(BusinessException.class, () -> validator.validate(config));
        verifyNoInteractions(userApi);
    }

    @Test
    void shouldValidateConfiguredFormData() {
        ApprovalFlowValidator validator = new ApprovalFlowValidator(userApi);
        FormConfigDTO config = validator.validateFormConfig(
            "{\"version\":1,\"fields\":[{\"id\":\"reason\",\"key\":\"reason\","
                + "\"label\":\"申请说明\",\"type\":\"TEXTAREA\",\"required\":true}]}");
        validator.validateFormData(config, "{\"reason\":\"需要审批\"}");
        validator.validateFormData(config, "{\"reason\":\"需要审批\",\"other_field\":\"业务补充\"}");
        assertThrows(BusinessException.class,
            () -> validator.validateFormData(config, "{\"未知字段\":\"不允许的编码\"}"));
    }

    @Test
    void shouldRejectSnapshotWithMissingCurrentNodeBeforeWriting() {
        instance.setFlowConfig(instance.getFlowConfig().replace("review", "replaced"));
        assertThrows(BusinessException.class, () -> service.approve(2L, new ApprovalOpinionReq()));
        assertEquals(ApprovalStatusEnum.PENDING, task.getStatus());
        verify(taskMapper, never()).updateById(any(ApprovalTaskDO.class));
        verifyNoInteractions(historyMapper);
    }
}
