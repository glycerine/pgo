package pgo.model.tla;

import pgo.util.SourceLocation;

public class PGoTLAFunctionDefinition extends PGoTLAUnit {
	
	PGoTLAIdentifier name;
	PGoTLAFunction function;
	boolean local;
	
	public PGoTLAFunctionDefinition(SourceLocation location, PGoTLAIdentifier name, PGoTLAFunction function, boolean isLocal) {
		super(location);
		this.name = name;
		this.function = function;
		this.local = isLocal;
	}

	public PGoTLAIdentifier getName() {
		return name;
	}

	public PGoTLAFunction getFunction() {
		return function;
	}
	
	public boolean isLocal() {
		return local;
	}

	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((function == null) ? 0 : function.hashCode());
		result = prime * result + (local ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		PGoTLAFunctionDefinition other = (PGoTLAFunctionDefinition) obj;
		if (function == null) {
			if (other.function != null)
				return false;
		} else if (!function.equals(other.function))
			return false;
		if (local != other.local)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
