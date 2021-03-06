package pgo.model.tla;

import java.util.List;
import java.util.stream.Collectors;

import pgo.util.SourceLocation;

/**
 * 
 * Represents a substitution in an EXCEPT expression.
 * 
 * For simplicity, the !.id version is represented as !["id"]. Both are formally equivalent.
 *
 */
public class PGoTLASubstitutionKey extends PGoTLANode {

	List<PGoTLAExpression> indices;
	
	public PGoTLASubstitutionKey(SourceLocation location, List<PGoTLAExpression> indices) {
		super(location);
		this.indices = indices;
	}
	
	@Override
	public PGoTLASubstitutionKey copy() {
		return new PGoTLASubstitutionKey(getLocation(), indices.stream().map(PGoTLAExpression::copy).collect(Collectors.toList()));
	}
	
	public List<PGoTLAExpression> getIndices(){
		return indices;
	}
	
	@Override
	public <T, E extends Throwable> T accept(PGoTLANodeVisitor<T, E> v) throws E {
		return v.visit(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((indices == null) ? 0 : indices.hashCode());
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
		PGoTLASubstitutionKey other = (PGoTLASubstitutionKey) obj;
		if (indices == null) {
			if (other.indices != null)
				return false;
		} else if (!indices.equals(other.indices))
			return false;
		return true;
	}

}
