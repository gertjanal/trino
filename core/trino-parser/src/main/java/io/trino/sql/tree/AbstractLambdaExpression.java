package io.trino.sql.tree;

public abstract class AbstractLambdaExpression
        extends Expression
{
    protected AbstractLambdaExpression(NodeLocation location)
    {
        super(location);
    }
}
