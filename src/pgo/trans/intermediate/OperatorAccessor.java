package pgo.trans.intermediate;

import java.util.List;
import java.util.Map;

import pgo.model.golang.BlockBuilder;
import pgo.model.golang.Expression;
import pgo.model.tla.PGoTLAExpression;
import pgo.model.type.PGoType;
import pgo.model.type.PGoTypeGenerator;
import pgo.model.type.PGoTypeSolver;
import pgo.model.type.PGoTypeVariable;
import pgo.scope.UID;
import pgo.util.Derived;
import pgo.util.DerivedVisitor;
import pgo.util.Origin;

public abstract class OperatorAccessor extends Derived {

	public abstract PGoType constrainTypes(Origin origin, DefinitionRegistry registry, List<PGoType> args, PGoTypeSolver solver,
	                                       PGoTypeGenerator generator, Map<UID, PGoTypeVariable> mapping);

	// TODO argument count mismatch
	public abstract int getArgumentCount();

	@Override
	public <T, E extends Throwable> T accept(DerivedVisitor<T, E> v) throws E {
		return v.visit(this);
	}

	public abstract UID getUID();

	public abstract Expression generateGo(BlockBuilder builder, PGoTLAExpression origin, DefinitionRegistry registry,
			List<PGoTLAExpression> args, Map<UID, PGoType> typeMap, GlobalVariableStrategy globalStrategy);

}
