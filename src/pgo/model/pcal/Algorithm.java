package pgo.model.pcal;

import java.util.List;
import java.util.stream.Collectors;

import pgo.model.tla.PGoTLAUnit;
import pgo.util.SourceLocation;

public class Algorithm extends Node {

	private String name;

	private List<VariableDeclaration> variables;
	private List<Macro> macros;
	private List<Procedure> procedures;
	private List<PGoTLAUnit> units;

	private Processes processes;

	public Algorithm(SourceLocation location, String name, List<VariableDeclaration> variables, List<Macro> macros,
	                 List<Procedure> procedures, List<PGoTLAUnit> units, Processes processes) {
		super(location);
		this.name = name;
		this.variables = variables;
		this.macros = macros;
		this.procedures = procedures;
		this.units = units;
		this.processes = processes;
	}

	@Override
	public Algorithm copy() {
		return new Algorithm(
				getLocation(),
				name,
				variables.stream().map(VariableDeclaration::copy).collect(Collectors.toList()),
				macros.stream().map(Macro::copy).collect(Collectors.toList()),
				procedures.stream().map(Procedure::copy).collect(Collectors.toList()),
				units.stream().map(PGoTLAUnit::copy).collect(Collectors.toList()),
				processes.copy());
	}

	public String getName() {
		return name;
	}

	public List<VariableDeclaration> getVariables() {
		return variables;
	}

	public List<Macro> getMacros() {
		return macros;
	}

	public List<Procedure> getProcedures() {
		return procedures;
	}

	public List<PGoTLAUnit> getUnits(){
		return units;
	}

	public Processes getProcesses() {
		return processes;
	}

	@Override
	public <T, E extends Throwable> T accept(NodeVisitor<T, E> v) throws E{
		return v.visit(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((macros == null) ? 0 : macros.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((procedures == null) ? 0 : procedures.hashCode());
		result = prime * result + ((processes == null) ? 0 : processes.hashCode());
		result = prime * result + ((units == null) ? 0 : units.hashCode());
		result = prime * result + ((variables == null) ? 0 : variables.hashCode());
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
		Algorithm other = (Algorithm) obj;
		if (macros == null) {
			if (other.macros != null)
				return false;
		} else if (!macros.equals(other.macros))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (procedures == null) {
			if (other.procedures != null)
				return false;
		} else if (!procedures.equals(other.procedures))
			return false;
		if (processes == null) {
			if (other.processes != null)
				return false;
		} else if (!processes.equals(other.processes))
			return false;
		if (units == null) {
			if (other.units != null)
				return false;
		} else if (!units.equals(other.units))
			return false;
		if (variables == null) {
			if (other.variables != null)
				return false;
		} else if (!variables.equals(other.variables))
			return false;
		return true;
	}

}
