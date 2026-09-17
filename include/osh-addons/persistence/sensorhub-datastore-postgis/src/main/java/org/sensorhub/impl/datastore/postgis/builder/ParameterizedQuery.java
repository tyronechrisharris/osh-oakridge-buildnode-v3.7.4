package org.sensorhub.impl.datastore.postgis.builder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public record ParameterizedQuery(String sql, List<Object> parameters) {
    public void bind(PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            preparedStatement.setObject(i + 1, parameters.get(i));
        }
    }
}
