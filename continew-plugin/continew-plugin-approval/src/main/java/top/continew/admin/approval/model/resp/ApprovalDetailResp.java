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

package top.continew.admin.approval.model.resp;

import lombok.Data;
import top.continew.admin.approval.model.entity.ApprovalHistoryDO;
import top.continew.admin.approval.model.entity.ApprovalInstanceDO;
import top.continew.admin.approval.model.entity.ApprovalTaskDO;

import java.util.List;
import java.util.Map;

/**
 * 当前用户参与的审批详情及审计历史。
 *
 * @author seewis
 * @since 5.0.0
 */
@Data
public class ApprovalDetailResp {

    private ApprovalInstanceDO instance;
    private List<ApprovalTaskDO> tasks;
    private List<ApprovalHistoryDO> history;
    private Map<Long, String> userNames;
}
