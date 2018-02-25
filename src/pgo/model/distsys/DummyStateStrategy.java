package pgo.model.distsys;

import pgo.model.golang.Expression;
import pgo.model.golang.GoProgram;
import pgo.model.golang.Statement;
import pgo.model.intermediate.PGoVariable;

import java.util.Vector;
import java.util.stream.Stream;

public class DummyStateStrategy implements StateStrategy {
	@Override
	public void generateConfig(GoProgram go) {
	}

	@Override
	public void generateGlobalVariables(GoProgram go) {
	}

	@Override
	public void initializeGlobalVariables(GoProgram go) {
	}

	@Override
	public void lock(int lockGroup, Vector<Statement> stmts, Stream<PGoVariable> vars) {
	}

	@Override
	public void unlock(int lockGroup, Vector<Statement> stmts, Stream<PGoVariable> vars) {
	}

	@Override
	public void setVar(PGoVariable var, Expression rhs, Vector<Expression> exps) {
	}

	@Override
	public String getVar(PGoVariable var) {
		return null;
	}
}
