package pgo.model.pcal;

import java.util.List;

import pgo.util.SourceLocation;

public class With extends Statement {
	
	VariableDecl variable;
	List<Statement> body;
	
	public With(SourceLocation location, VariableDecl variable, List<Statement> body) {
		super(location);
		this.variable = variable;
		this.body = body;
	}
	
	public VariableDecl getVariable() {
		return variable;
	}
	
	public List<Statement> getBody(){
		return body;
	}

	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result + ((variable == null) ? 0 : variable.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		With other = (With) obj;
		if (body == null) {
			if (other.body != null)
				return false;
		} else if (!body.equals(other.body))
			return false;
		if (variable == null) {
			if (other.variable != null)
				return false;
		} else if (!variable.equals(other.variable))
			return false;
		return true;
	}

}
