/*
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
package io.trino.sql.planner.iterative.rule;

import io.trino.matching.Captures;
import io.trino.matching.Pattern;
import io.trino.metadata.Metadata;
import io.trino.metadata.TableProperties;
import io.trino.spi.TrinoException;
import io.trino.spi.connector.ColumnHandle;
import io.trino.spi.predicate.Domain;
import io.trino.spi.predicate.TupleDomain;
import io.trino.sql.planner.iterative.Rule;
import io.trino.sql.planner.plan.TableScanNode;

import java.util.List;
import java.util.Map;

import static io.trino.spi.StandardErrorCode.QUERY_REJECTED;
import static io.trino.sql.planner.plan.Patterns.tableScan;
import static java.util.stream.Collectors.joining;

public class EnforceFullPartitionFilter
        implements Rule<TableScanNode>
{
    private final Metadata metadata;

    public EnforceFullPartitionFilter(Metadata metadata)
    {
        this.metadata = metadata;
    }

    @Override
    public Pattern<TableScanNode> getPattern()
    {
        return tableScan();
    }

    @Override
    public Result apply(TableScanNode node, Captures captures, Context context)
    {
        TableProperties props = metadata.getTableProperties(context.getSession(), node.getTable());
        props.getTablePartitioning().ifPresent(partitioning -> {
            List<ColumnHandle> partitionColumns = partitioning.partitioningColumns();

            if (!isFullyConstrained(node.getEnforcedConstraint(), partitionColumns)) {
                throw new TrinoException(QUERY_REJECTED, "Query must filter on all partition columns: " + partitionColumns.stream()
                        .map(ColumnHandle::toString)
                        .collect(joining(", ")));
            }
        });
        return Result.empty();
    }

    private boolean isFullyConstrained(
            TupleDomain<ColumnHandle> constraint,
            List<ColumnHandle> partitionColumns)
    {
        if (constraint.isAll()) {
            return false;
        }

        Map<ColumnHandle, Domain> domains = constraint.getDomains().orElse(Map.of());
        for (ColumnHandle col : partitionColumns) {
            if (!domains.containsKey(col) || domains.get(col).isAll()) {
                return false;
            }
        }
        return true;
    }
}
