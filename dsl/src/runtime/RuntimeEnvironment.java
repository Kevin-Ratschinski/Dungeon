package runtime;

import interpreter.DSLInterpreter;

import semanticanalysis.IScope;
import semanticanalysis.Symbol;
import semanticanalysis.SymbolTable;
import semanticanalysis.types.IType;
import semanticanalysis.types.TypeBuilder;
import semanticanalysis.types.TypeInstantiator;

import java.lang.reflect.Type;
import java.util.HashMap;

// this extends the normal IEnvironment definition by storing prototypes
// which are basically evaluated type definitions (of game objects)
public class RuntimeEnvironment implements IEvironment {
    private final SymbolTable symbolTable;
    private final HashMap<String, Symbol> functions;
    private final HashMap<String, Prototype> prototypes;
    private final HashMap<Type, IType> javaTypeToDSLType;
    private final RuntimeObjectTranslator runtimeObjectTranslator;
    private final TypeBuilder typeBuilder;
    private final TypeInstantiator typeInstantiator;

    public RuntimeObjectTranslator getRuntimeObjectTranslator() {
        return runtimeObjectTranslator;
    }

    /**
     * Constructor. Create new runtime environment from an existing environment and add all type
     * definitions to the stored types.
     *
     * @param other the other environment to create a new RuntimeEnvironment from
     */
    public RuntimeEnvironment(IEvironment other, DSLInterpreter interpreter) {
        this.symbolTable = other.getSymbolTable();
        this.typeBuilder = other.getTypeBuilder();

        var functions = other.getFunctions();
        this.functions = new HashMap<>();
        for (var function : functions) {
            this.functions.put(function.getName(), function);
        }

        this.prototypes = new HashMap<>();

        this.javaTypeToDSLType = other.javaTypeToDSLTypeMap();

        this.runtimeObjectTranslator = other.getRuntimeObjectTranslator();
        this.typeInstantiator = new TypeInstantiator(interpreter);
    }

    /**
     * Lookup a {@link Prototype} with name
     *
     * @param name the name of the Prototype to lookup
     * @return the Prototype with the passed name or Prototype.NONE
     */
    public Prototype lookupPrototype(String name) {
        return this.prototypes.getOrDefault(name, Prototype.NONE);
    }

    /**
     * Add new {@link Prototype}
     *
     * @param prototype the new Prototype
     * @return true on success, false otherwise
     */
    public boolean addPrototype(Prototype prototype) {
        if (this.prototypes.containsKey(prototype.getName())) {
            return false;
        } else {
            this.prototypes.put(prototype.getName(), prototype);
            return true;
        }
    }

    @Override
    public TypeBuilder getTypeBuilder() {
        return this.typeBuilder;
    }

    @Override
    public SymbolTable getSymbolTable() {
        return this.symbolTable;
    }

    @Override
    public IScope getGlobalScope() {
        return this.symbolTable.getGlobalScope();
    }

    @Override
    public HashMap<Type, IType> javaTypeToDSLTypeMap() {
        return this.javaTypeToDSLType;
    }

    public Object translateRuntimeObject(Object object, IMemorySpace parentMemorySpace) {
        return this.runtimeObjectTranslator.translateRuntimeObject(object, parentMemorySpace, this);
    }

    public Object translateRuntimeObject(
            Object object, IMemorySpace parentMemorySpace, IType targetType) {
        return this.runtimeObjectTranslator.translateRuntimeObject(
                object, parentMemorySpace, this, targetType);
    }

    public TypeInstantiator getTypeInstantiator() {
        return this.typeInstantiator;
    }
}
