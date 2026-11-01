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
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.continew.admin.approval.enums.ApproverTypeEnum;
import top.continew.admin.approval.model.dto.FlowConfigDTO;
import top.continew.admin.approval.model.dto.FlowNodeDTO;
import top.continew.admin.approval.model.dto.FormConfigDTO;
import top.continew.admin.approval.model.dto.FormFieldDTO;
import top.continew.admin.approval.model.dto.FormOptionDTO;
import top.continew.admin.approval.model.dto.FormValidationDTO;
import top.continew.admin.common.api.system.UserApi;
import top.continew.starter.core.util.validation.CheckUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 定义保存与发起审批共用的流程校验。
 *
 * @author seewis
 * @since 5.0.0
 */
@Component
@RequiredArgsConstructor
public class ApprovalFlowValidator {

    private static final String DEFAULT_FORM_CONFIG = "{\"version\":1,\"fields\":["
        + "{\"id\":\"reason\",\"key\":\"reason\",\"label\":\"申请说明\","
        + "\"type\":\"TEXTAREA\",\"required\":true,\"validation\":{\"maxLength\":1000}}]}";

    private static final Set<String> FORM_TYPES = Set.of(
        "TEXT", "TEXTAREA", "NUMBER", "AMOUNT", "DATE", "DATERANGE", "SELECT", "RADIO");

    private final UserApi userApi;

    /**
     * 校验可执行配置和有效审批人。
     * @param json 流程配置
     * @return 已校验的配置
     */
    public FlowConfigDTO validate(String json) {
        CheckUtils.throwIf(!JSONUtil.isTypeJSONObject(json), "流程配置必须是 JSON 对象");
        FlowConfigDTO config = JSONUtil.toBean(json, FlowConfigDTO.class);
        CheckUtils.throwIfEmpty(config.getNodes(), "请至少添加一个审批节点");
        Set<String> ids = new HashSet<>();
        Set<Long> checkedUsers = new HashSet<>();
        for (FlowNodeDTO node : config.getNodes()) {
            CheckUtils.throwIfNull(node, "审批节点不能为空");
            CheckUtils.throwIf(StrUtil.isBlank(node.getId()) || node.getId().length() > 50,
                "节点编号不能为空且长度不能超过 50");
            CheckUtils.throwIf(!ids.add(node.getId()), "节点编号不能重复");
            CheckUtils.throwIf(StrUtil.isBlank(node.getName()) || node.getName().length() > 100,
                "节点名称不能为空且长度不能超过 100");
            CheckUtils.throwIfNotEqual(node.getType(), "APPROVAL", "仅支持审批节点");
            CheckUtils.throwIfNotEqual(node.getApproverType(), ApproverTypeEnum.USER, "当前仅支持指定用户");
            CheckUtils.throwIfNull(node.getApproveMode(), "请选择审批模式");
            CheckUtils.throwIfEmpty(node.getApprovers(), "每个节点至少选择一名审批人");
            CheckUtils.throwIf(
                new HashSet<>(node.getApprovers()).size() != node.getApprovers().size(),
                "审批人不能重复");
            for (Long id : node.getApprovers()) {
                if (checkedUsers.add(id)) {
                    CheckUtils.throwIf(!userApi.isAvailable(id), "审批人不存在或已禁用");
                }
            }
        }
        return config;
    }

    /**
     * 校验申请表单配置。
     *
     * @param json 表单配置
     * @return 已校验的配置
     */
    public FormConfigDTO validateFormConfig(String json) {
        String value = StrUtil.isBlank(json) ? DEFAULT_FORM_CONFIG : json;
        CheckUtils.throwIf(!JSONUtil.isTypeJSONObject(value), "表单配置必须是 JSON 对象");
        FormConfigDTO config = JSONUtil.toBean(value, FormConfigDTO.class);
        CheckUtils.throwIfNull(config.getVersion(), "表单配置版本不能为空");
        CheckUtils.throwIfNotEqual(config.getVersion(), 1, "暂不支持此表单配置版本");
        CheckUtils.throwIfEmpty(config.getFields(), "请至少添加一个申请字段");
        CheckUtils.throwIf(config.getFields().size() > 30, "申请字段不能超过 30 个");
        Set<String> keys = new HashSet<>();
        Set<String> ids = new HashSet<>();
        for (FormFieldDTO field : config.getFields()) {
            CheckUtils.throwIfNull(field, "申请字段不能为空");
            CheckUtils.throwIf(StrUtil.isBlank(field.getId())
                || !field.getId().matches("[A-Za-z][A-Za-z0-9_]{0,49}"),
                "字段编号必须以字母开头，仅包含字母、数字和下划线，长度不超过 50");
            CheckUtils.throwIf(StrUtil.isBlank(field.getKey())
                || !field.getKey().matches("[A-Za-z][A-Za-z0-9_]{0,49}"),
                "字段编码必须以字母开头，仅包含字母、数字和下划线，长度不超过 50");
            CheckUtils.throwIf(!ids.add(field.getId()), "字段编号不能重复");
            CheckUtils.throwIf(!keys.add(field.getKey()), "字段编码不能重复");
            CheckUtils.throwIf(StrUtil.isBlank(field.getLabel()) || field.getLabel().length() > 100,
                "字段名称不能为空且长度不能超过 100");
            CheckUtils.throwIf(!FORM_TYPES.contains(field.getType()), "不支持的字段类型");
            FormValidationDTO validation = field.getValidation();
            if (validation != null) {
                CheckUtils.throwIf(
                    validation.getMinLength() != null && validation.getMinLength() < 0,
                    "最小长度不能小于 0");
                CheckUtils.throwIf(
                    validation.getMaxLength() != null && validation.getMaxLength() < 1,
                    "最大长度必须大于 0");
                CheckUtils
                    .throwIf(validation.getMinLength() != null && validation.getMaxLength() != null
                        && validation.getMinLength() > validation.getMaxLength(), "长度范围无效");
                CheckUtils.throwIf(
                    validation.getMaxLength() != null && validation.getMaxLength() > 5000,
                    "最大长度不能超过 5000");
                CheckUtils.throwIf(validation.getMin() != null && validation.getMax() != null
                    && validation.getMin().compareTo(validation.getMax()) > 0, "数值范围无效");
            }
            if ("SELECT".equals(field.getType()) || "RADIO".equals(field.getType())) {
                CheckUtils.throwIfEmpty(field.getOptions(), "选择类字段必须配置选项");
                CheckUtils.throwIf(field.getOptions().size() > 50, "字段选项不能超过 50 个");
                Set<String> values = new HashSet<>();
                for (FormOptionDTO option : field.getOptions()) {
                    CheckUtils.throwIfNull(option, "字段选项不能为空");
                    CheckUtils.throwIf(
                        StrUtil.isBlank(option.getLabel()) || option.getLabel().length() > 100,
                        "选项名称不能为空且长度不能超过 100");
                    CheckUtils.throwIf(
                        StrUtil.isBlank(option.getValue()) || option.getValue().length() > 100,
                        "选项值不能为空且长度不能超过 100");
                    CheckUtils.throwIf(!values.add(option.getValue()), "选项值不能重复");
                }
            }
        }
        return config;
    }

    /**
     * 校验申请数据与表单配置一致。
     *
     * @param formConfig 表单配置
     * @param formData 申请数据
     */
    public void validateFormData(FormConfigDTO formConfig, String formData) {
        CheckUtils.throwIfBlank(formData, "请填写申请内容");
        CheckUtils.throwIf(formData.length() > 200_000, "申请内容不能超过 200 KB");
        CheckUtils.throwIf(!JSONUtil.isTypeJSONObject(formData), "申请内容必须是 JSON 对象");
        JSONObject data = JSONUtil.parseObj(formData);
        Map<String, FormFieldDTO> fields = formConfig.getFields().stream()
            .collect(Collectors.toMap(FormFieldDTO::getKey, field -> field));
        CheckUtils.throwIf(data.size() > 80, "申请内容字段不能超过 80 个");
        data.keySet().forEach(key -> CheckUtils.throwIf(!key.matches("[A-Za-z][A-Za-z0-9_]{0,49}"),
            "申请内容字段编码无效"));
        for (FormFieldDTO field : formConfig.getFields()) {
            Object value = data.get(field.getKey());
            boolean blank =
                value == null || (value instanceof String && StrUtil.isBlank((String) value));
            if (Boolean.TRUE.equals(field.getRequired())) {
                CheckUtils.throwIf(blank, "请填写：{}", field.getLabel());
            }
            if (blank) {
                continue;
            }
            if ("TEXT".equals(field.getType()) || "TEXTAREA".equals(field.getType())) {
                CheckUtils.throwIf(!(value instanceof String), "字段 {} 必须是文本", field.getLabel());
                validateText(field, (String) value);
            } else if ("NUMBER".equals(field.getType()) || "AMOUNT".equals(field.getType())) {
                CheckUtils.throwIf(!(value instanceof Number), "字段 {} 必须是数字", field.getLabel());
                validateNumber(field, ((Number) value).doubleValue());
            } else if ("SELECT".equals(field.getType()) || "RADIO".equals(field.getType())) {
                String selected = String.valueOf(value);
                CheckUtils.throwIf(
                    field.getOptions().stream().noneMatch(o -> selected.equals(o.getValue())),
                    "字段 {} 的选项无效", field.getLabel());
            } else if ("DATERANGE".equals(field.getType())) {
                CheckUtils.throwIf(
                    !(value instanceof java.util.List) || ((java.util.List<?>) value).size() != 2,
                    "字段 {} 必须是日期范围", field.getLabel());
            } else {
                CheckUtils.throwIf(!(value instanceof String) && !(value instanceof Number),
                    "字段 {} 必须是日期", field.getLabel());
            }
        }
    }

    /**
     * 返回旧定义可使用的默认配置。
     *
     * @return 默认配置 JSON
     */
    public String defaultFormConfig() {
        return DEFAULT_FORM_CONFIG;
    }

    private void validateText(FormFieldDTO field, String value) {
        FormValidationDTO validation = field.getValidation();
        if (validation == null) {
            return;
        }
        CheckUtils.throwIf(
            validation.getMinLength() != null && value.length() < validation.getMinLength(),
            "字段 {} 长度不能少于 {}", field.getLabel(), validation.getMinLength());
        CheckUtils.throwIf(
            validation.getMaxLength() != null && value.length() > validation.getMaxLength(),
            "字段 {} 长度不能超过 {}", field.getLabel(), validation.getMaxLength());
    }

    private void validateNumber(FormFieldDTO field, double value) {
        FormValidationDTO validation = field.getValidation();
        if (validation == null) {
            return;
        }
        CheckUtils.throwIf(validation.getMin() != null && value < validation.getMin().doubleValue(),
            "字段 {} 不能小于 {}", field.getLabel(), validation.getMin());
        CheckUtils.throwIf(validation.getMax() != null && value > validation.getMax().doubleValue(),
            "字段 {} 不能大于 {}", field.getLabel(), validation.getMax());
    }
}
