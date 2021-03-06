package pgo.model.pcal;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import pgo.util.SourceLocation;

public class Macro extends Node {
	
	String name;
	List<String> params;
	List<Statement> body;
	
	public Macro(SourceLocation location, String name, List<String> params, List<Statement> body) {
		super(location);
		this.name = name;
		this.params = params;
		this.body = body;
	}
	
	@Override
	public Macro copy() {
		return new Macro(
				getLocation(),
				name,
				new ArrayList<>(params),
				body.stream().map(Statement::copy).collect(Collectors.toList()));
	}
	
	public String getName() {
		return name;
	}
	
	public List<String> getParams(){
		return params;
	}
	
	public List<Statement> getBody(){
		return body;
	}
	
	@Override
	public <T, E extends Throwable> T accept(NodeVisitor<T, E> v) throws E{
		return v.visit(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((params == null) ? 0 : params.hashCode());
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
		Macro other = (Macro) obj;
		if (body == null) {
			if (other.body != null)
				return false;
		} else if (!body.equals(other.body))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		return true;
	}

}
