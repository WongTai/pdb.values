package org.eclipse.imp.pdb.facts.type;

import java.util.HashMap;
import java.util.Map;

/**
 * Use this class to substitute type parameters for other types.
 */
public class TypeInstantiator implements ITypeVisitor<Type> {
	private final Type[] fActuals;
	private int fActualIndex;
	private final Map<Type, Type> fBindings;
	private final TypeFactory tf = TypeFactory.getInstance();

	public TypeInstantiator(Type... actuals) {
		fActuals = actuals;
		fBindings = new HashMap<Type,Type>();
		fActualIndex = 0;
	}
	
	/**
	 * Instantiate a parameterized type with actual types
	 * @param abstractType the type to find embedded parameterized types in
	 * @param actuals      the actual types to replace the parameterized types with
	 * @return a new type with the parameter types replaced by the given actual types.
	 */
	public Type instantiate(Type abstractType, Type... actuals) {
		return abstractType.accept(this);
	}
	
	public Type visitBool(BoolType boolType) {
		return boolType;
	}

	public Type visitDouble(DoubleType type) {
		return type;
	}

	public Type visitInteger(IntegerType type) {
		return type;
	}

	public Type visitList(ListType type) {
		return tf.listType(type.getElementType().accept(this));
	}

	public Type visitMap(MapType type) {
		return tf.mapType(type.getKeyType().accept(this), type.getValueType().accept(this));
	}

	public Type visitNamed(NamedType type) {
		return type;
	}

	public Type visitNamedTree(NamedTreeType type) {
		return type;
	}

	public Type visitParameter(ParameterType parameterType) {
		Type boundTo = fBindings.get(parameterType);
		if (boundTo == null) {
			if (fActualIndex >= fActuals.length) {
				throw new FactTypeError("Not enough actual types to instantiate " + parameterType);
			}
			boundTo = fActuals[fActualIndex++];
			fBindings.put(parameterType, boundTo);
		}
		return boundTo;
	}

	public Type visitRelationType(RelationType type) {
		return tf.relType(type.getFieldTypes().accept(this));
	}

	public Type visitSet(SetType type) {
		return tf.setType(type.getElementType().accept(this));
	}

	public Type visitSourceLocation(SourceLocationType type) {
		return type;
	}

	public Type visitSourceRange(SourceRangeType type) {
		return type;
	}

	public Type visitString(StringType type) {
		return type;
	}

	public Type visitTree(TreeType type) {
		return type;
	}

	public Type visitTreeNode(TreeNodeType type) {
		return tf.treeNodeType((NamedTreeType) type.getSuperType().accept(this), type.getName(), type.getChildrenTypes().accept(this));
	}

	public Type visitTuple(TupleType type) {
		if (type.hasFieldNames()) {
		  Object[] fChildren = new Type[2 * type.getArity()];
		
		  for (int i = 0, j = 0; i < fChildren.length; i++, j++) {
			 fChildren[i++] = type.getFieldType(j).accept(this);
			 fChildren[i] = type.getFieldName(i);
		  }
		  
		  return tf.tupleType(fChildren);
		}
		else {
			Type[] fChildren = new Type[type.getArity()];
			for (int i = 0; i < fChildren.length; i++) {
				fChildren[i] = type.getFieldType(i).accept(this);
			}
			
			return tf.tupleType(fChildren);
		}
	}

	public Type visitValue(ValueType type) {
		return type;
	}

	public Type visitVoid(VoidType type) {
		return type;
	}
}