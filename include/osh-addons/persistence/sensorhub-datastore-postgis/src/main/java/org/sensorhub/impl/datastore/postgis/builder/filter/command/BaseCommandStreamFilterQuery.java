/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are subject to the Mozilla Public License, v. 2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one
 at http://mozilla.org/MPL/2.0/.

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.

 Copyright (C) 2023 Georobotix. All Rights Reserved.

 ******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore.postgis.builder.filter.command;

import org.sensorhub.api.datastore.FullTextFilter;
import org.sensorhub.api.datastore.TemporalFilter;
import org.sensorhub.api.datastore.command.CommandFilter;
import org.sensorhub.api.datastore.command.CommandStreamFilter;
import org.sensorhub.api.datastore.system.SystemFilter;
import org.sensorhub.impl.datastore.postgis.builder.filter.FilterQuery;
import org.sensorhub.impl.datastore.postgis.builder.generator.FilterQueryGenerator;

import java.util.SortedSet;
import java.util.stream.Collectors;

public abstract class BaseCommandStreamFilterQuery<F extends FilterQueryGenerator> extends FilterQuery<F> {

    protected BaseCommandStreamFilterQuery(String tableName, F filterQueryGenerator) {
        super(tableName, filterQueryGenerator);
    }

    public F build(CommandStreamFilter filter) {
        this.handleInternalIDs(filter.getInternalIDs());
        this.handleControlInputNames(filter.getControlInputNames());
        this.handleValidTimeFilter(filter.getValidTimeFilter());
        this.handleFullTextFilter(filter.getFullTextFilter());
        this.handleCommandFilter(filter.getCommandFilter());
        this.handleSystemFilter(filter.getSystemFilter());
        this.handleTaskableProperties(filter.getTaskableProperties());
        return this.filterQueryGenerator;
    }

    protected  void handleControlInputNames(SortedSet<String> names) {
        if (names != null && !names.isEmpty()) {
            addCondition("("+this.tableName+".data->>'name') in (" + placeholders(names.size()) + ")");
            names.forEach(this::addParameter);
        }
    }

    protected abstract void handleValidTimeFilter(TemporalFilter temporalFilter);

    protected void handleFullTextFilter(FullTextFilter fullTextFilter) {
        if (fullTextFilter != null) {
            String sb = "(" + this.tableName + ".data->'recordSchema'->>'description') ~* ?";
            addCondition(sb);
            addParameter("(" + fullTextFilter.getKeywords().stream().collect(Collectors.joining("|")) + ")");
        }
    }

    protected abstract void handleCommandFilter(CommandFilter commandFilter);

    protected void handleSystemFilter(SystemFilter systemFilter) {
        if(systemFilter != null) {
            if(this.sysDescTableName != null) {
                // create JOIN
                // TODO
                throw new UnsupportedOperationException();
            } else {
                // otherwise
                if (systemFilter.getInternalIDs() != null || systemFilter.getUniqueIDs() != null) {
                    // handle UNIQUE IDS
                    if (systemFilter.getUniqueIDs() != null && !systemFilter.getUniqueIDs().isEmpty()) {
                        String sb = "(" + tableName + ".data->'system@id'->>'uniqueID') in (" +
                                placeholders(systemFilter.getUniqueIDs().size()) + ")";
                        addCondition(sb);
                        systemFilter.getUniqueIDs().forEach(this::addParameter);
                    }

                    // handle internal IDS
                    if (systemFilter.getInternalIDs() != null && !systemFilter.getInternalIDs().isEmpty()) {
                        StringBuilder sb = new StringBuilder("(");

                        if (systemFilter.includeMembers()) {
                            // Use join
                            addJoin(sysDescTableName + " s ON (" + tableName +
                                    ".data->'system@id'->'internalID'->>'id')::bigint = s.id");

                            sb.append("s.id IN (").append(placeholders(systemFilter.getInternalIDs().size())).append(")")
                                    .append(" OR s.parentid IN (").append(placeholders(systemFilter.getInternalIDs().size())).append(")");
                            systemFilter.getInternalIDs().forEach(bigId -> addParameter(bigId.getIdAsLong()));
                            systemFilter.getInternalIDs().forEach(bigId -> addParameter(bigId.getIdAsLong()));
                        } else {
                            sb.append("(").append(tableName)
                                    .append(".data->'system@id'->'internalID'->>'id')::bigint IN (")
                                    .append(placeholders(systemFilter.getInternalIDs().size())).append(")");
                            systemFilter.getInternalIDs().forEach(bigId -> addParameter(bigId.getIdAsLong()));
                        }

                        sb.append(")");
                        addCondition(sb.toString());
                    }
                }
                if (systemFilter.getParentFilter() != null || systemFilter.getProcedureFilter() != null
                        || systemFilter.getDataStreamFilter() != null || systemFilter.getFullTextFilter() != null
                        || systemFilter.getLocationFilter() != null || systemFilter.getValidTime() != null) {
                    throw new IllegalStateException("No linked system store");
                }
            }
        }
    }

    protected void handleTaskableProperties(SortedSet<String> properties) {
        if(properties != null) {
            StringBuilder jsonPath = new StringBuilder();
            jsonPath.append("$.** ? (");
            boolean first=true;
            for(String property : properties) {
                if(!first) {
                    // prepend operator
                    jsonPath.append(" || ");
                }
                jsonPath.append("@ == \"").append(escapeJsonString(property)).append("\"");
                first = false;
            }
            jsonPath.append(")");
            addCondition("jsonb_path_exists(" + tableName + ".data, ?::jsonpath)");
            addParameter(jsonPath.toString());
        }
    }

    protected String escapeJsonString(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
