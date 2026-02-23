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
import com.google.common.primitives.Primitives;
import io.trino.metadata.SqlScalarFunction;
import io.trino.spi.block.Block;
import io.trino.spi.block.RowBlock;
import io.trino.spi.block.SqlRow;
import io.trino.spi.function.BoundSignature;
import io.trino.spi.function.FunctionMetadata;
import io.trino.spi.function.Signature;
import io.trino.spi.type.RowType;
import io.trino.spi.type.TypeSignature;
import io.trino.spi.type.VarcharType;
import io.trino.sql.gen.lambda.UnaryFunctionInterface;

import java.lang.invoke.MethodHandle;
import java.util.Optional;

import static io.trino.spi.function.InvocationConvention.InvocationArgumentConvention.*;
import static io.trino.spi.function.InvocationConvention.InvocationReturnConvention.FAIL_ON_NULL;
import static io.trino.spi.type.TypeSignature.functionType;
import static io.trino.util.Reflection.methodHandle;

public class RowTransformer
        extends SqlScalarFunction
{
    public static final RowTransformer ROW_TRANSFORMER = new RowTransformer();
    private static final MethodHandle METHOD_HANDLE = methodHandle(RowTransformer.class, "switchCase", SqlRow.class, UnaryFunctionInterface.class);

    private RowTransformer()
    {
        super(FunctionMetadata.scalarBuilder("transformer")
                .description("TODO")
                .signature(Signature.builder()
                        .rowTypeParameter("T")
                        .returnType(new TypeSignature("T"))
                        .argumentType(new TypeSignature("T"))
                        .argumentType(functionType(new TypeSignature("T"), new TypeSignature("T")))
                        .build())
                .build());
    }

    @Override
    public SpecializedSqlScalarFunction specialize(BoundSignature boundSignature)
    {
        Class<?> valueType = boundSignature.getArgumentType(0).getJavaType();
        Class<?> returnType = boundSignature.getReturnType().getJavaType();

        return new ChoicesSpecializedSqlScalarFunction(
                boundSignature,
                FAIL_ON_NULL,
                ImmutableList.of(NEVER_NULL, FUNCTION),
                ImmutableList.of(UnaryFunctionInterface.class),
METHOD_HANDLE,
//                METHOD_HANDLE.asType(
//                        METHOD_HANDLE.type()
//                                .changeParameterType(0, Primitives.wrap(valueType))
//                                .changeReturnType(Primitives.unwrap(returnType))),
                Optional.empty());
    }

    public static SqlRow switchCase(SqlRow value, UnaryFunctionInterface function)
    {
        Object ret = function.apply(value);
        return (SqlRow) ret;
    }
}
