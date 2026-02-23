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
package io.trino.sql.tree;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public final class MultilineLambdaExpression
        extends AbstractLambdaExpression
{
    private final List<LambdaArgumentDeclaration> arguments;
    private final List<ControlStatement> statements;

    public MultilineLambdaExpression(NodeLocation location, List<LambdaArgumentDeclaration> arguments, ControlStatement statement)
    {
        super(location);
        this.arguments = requireNonNull(arguments, "arguments is null");
        requireNonNull(statement, "statement is null");
        this.statements = statement instanceof CompoundStatement c ? c.getStatements() : ImmutableList.of(statement);
        if (this.statements.isEmpty()) {
            // TODO should this be IAE?
            throw new IllegalArgumentException("statement is empty");
        }
    }

    public List<LambdaArgumentDeclaration> getArguments()
    {
        return arguments;
    }

    public List<ControlStatement> getStatements()
    {
        return statements;
    }

    public Expression getReturnType()
    {
        List<ControlStatement> statements = getStatements();
        if (statements.getLast() instanceof ReturnStatement s) {
            return s.getValue();
        }
        // TODO should this be ISE?
        throw new IllegalStateException("Unexpected statement type: " + statements.getLast().getClass());
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context)
    {
        return visitor.visitMultilineLambdaExpression(this, context);
    }

    @Override
    public List<Node> getChildren()
    {
        ImmutableList.Builder<Node> nodes = ImmutableList.builder();
        nodes.addAll(arguments);
        nodes.addAll(statements);
        return nodes.build();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MultilineLambdaExpression that = (MultilineLambdaExpression) obj;
        return Objects.equals(arguments, that.arguments) &&
                Objects.equals(statements, that.statements);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(arguments, statements);
    }

    @Override
    public boolean shallowEquals(Node other)
    {
        return sameClass(this, other);
    }
}
