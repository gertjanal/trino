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
package io.trino.operator.scalar;

import com.google.common.collect.ImmutableList;
import io.airlift.slice.Slice;
import io.airlift.slice.Slices;
import io.trino.spi.TrinoException;
import io.trino.sql.query.QueryAssertions;
import io.trino.type.LikePattern;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;

import java.util.Optional;

import static io.airlift.slice.Slices.utf8Slice;
import static io.trino.type.LikeFunctions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@TestInstance(PER_CLASS)
@Execution(CONCURRENT)
public class TestRowTransformer
{
    private QueryAssertions assertions;

    @BeforeAll
    public void init()
    {
        assertions = new QueryAssertions();
    }

    @AfterAll
    public void teardown()
    {
        if (assertions != null) {
            assertions.close();
        }
        assertions = null;
    }

    @Test
    public void testReturnStatement()
    {
        // value and pattern with explicit type (formal type potentially longer than actual length)
        assertThat(assertions.expression("transformer(data, t -> RETURN t)")
                .binding("data", "CAST(ROW('hello') AS ROW(greeting varchar))"))
                .isEqualTo(ImmutableList.of("hello"));
    }

    @Test
    public void testSingleCompoundStatement()
    {
        // value and pattern with explicit type (formal type potentially longer than actual length)
        assertThat(assertions.expression("transformer(data, t -> BEGIN RETURN t; END)")
                .binding("data", "CAST(ROW('hello') AS ROW(greeting varchar))"))
                .isEqualTo(ImmutableList.of("hello"));
    }
}
