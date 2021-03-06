package pgo.model.type;

import pgo.util.Origin;

import java.util.List;

/**
 * Represents a slice.
 */
public class PGoTypeSlice extends PGoSimpleContainerType {
	public PGoTypeSlice(PGoType elementType, List<Origin> origins) {
		super(elementType, origins);
	}

	@Override
	public boolean equals(Object p) {
		if (!(p instanceof PGoTypeSlice)) {
			return false;
		}
		return super.equals(p);
	}

	@Override
	public String toTypeName() {
		return "Slice[" + elementType.toTypeName() + "]";
	}

	@Override
	public PGoType copy() {
		return new PGoTypeSlice(elementType.copy(), getOrigins());
	}

	@Override
	public <T, E extends Throwable> T accept(PGoTypeVisitor<T, E> v) throws E {
		return v.visit(this);
	}
}
