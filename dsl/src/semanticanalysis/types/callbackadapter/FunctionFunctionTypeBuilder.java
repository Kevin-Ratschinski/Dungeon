package semanticanalysis.types.callbackadapter;

import semanticanalysis.IScope;
import semanticanalysis.types.FunctionType;
import semanticanalysis.types.IType;
import semanticanalysis.types.TypeBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Builder for a {@link FunctionType} for a callback defined by the {@link
 * java.util.function.Function} interface
 */
public class FunctionFunctionTypeBuilder implements IFunctionTypeBuilder {

    public static FunctionFunctionTypeBuilder instance = new FunctionFunctionTypeBuilder();

    private FunctionFunctionTypeBuilder() {}

    @Override
    public FunctionType buildFunctionType(
            Field field, TypeBuilder typeBuilder, IScope globalScope) {
        var genericType = field.getGenericType();

        var parameterizedType = (ParameterizedType) genericType;
        Type[] typeArray = parameterizedType.getActualTypeArguments();

        // the first type parameter of the Function<T,R> interface will correspond to
        // the type of the single parameter of the function
        Type parameterType = typeArray[0];
        IType parameterDSLType =
                typeBuilder.createDSLTypeForJavaTypeInScope(globalScope, parameterType);
        if (null == parameterDSLType) {
            throw new RuntimeException("Type of parameter of Function could not be translated");
        }

        // the second type parameter of the Function<T,R> interface will correspond to
        // the return type of the function
        Type returnType = typeArray[1];
        IType returnDSLType = typeBuilder.createDSLTypeForJavaTypeInScope(globalScope, returnType);

        if (null == returnDSLType) {
            throw new RuntimeException("Returntype of Function could not be translated");
        }
        return new FunctionType(returnDSLType, parameterDSLType);
    }
}
