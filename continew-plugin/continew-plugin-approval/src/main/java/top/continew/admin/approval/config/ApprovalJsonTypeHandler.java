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

package top.continew.admin.approval.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * 审批 JSON 字符串保持原始 JSON，兼容 PostgreSQL JSONB 与 MySQL JSON。
 *
 * @author seewis
 * @since 5.0.0
 */
public class ApprovalJsonTypeHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int index, String parameter,
        JdbcType jdbcType) throws SQLException {
        if ("PostgreSQL".equals(ps.getConnection().getMetaData().getDatabaseProductName())) {
            ps.setObject(index, parameter, Types.OTHER);
        } else {
            ps.setString(index, parameter);
        }
    }

    @Override
    public String getNullableResult(ResultSet rs, String name) throws SQLException {
        return rs.getString(name);
    }

    @Override
    public String getNullableResult(ResultSet rs, int index) throws SQLException {
        return rs.getString(index);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int index) throws SQLException {
        return cs.getString(index);
    }
}
