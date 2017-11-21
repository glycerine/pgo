package pgo.trans.intermediate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import pgo.PGoNetOptions;
import pgo.model.intermediate.PGoFunction;
import pgo.model.intermediate.PGoLibFunction;
import pgo.model.intermediate.PGoType;
import pgo.model.intermediate.PGoVariable;
import pgo.model.tla.PGoTLADefinition;

/**
 * A class that holds the same data as the PGoTransIntermediateData, in addition
 * to variables that are local to some scope (e.g. in a with clause or set
 * declaration). Intended for use in classes which need variable information
 * such as the TLAExprToType class.
 *
 */
public class PGoTempData extends PGoTransIntermediateData {
	// The local variables
	private LinkedHashMap<String, PGoVariable> locals;
	// Contains some TLA constants like "Nat" (the set of naturals).
	private static final LinkedHashMap<String, PGoVariable> constants = new LinkedHashMap<String, PGoVariable>() {
		{
			put("Nat", PGoVariable.convert("Nat", PGoType.inferFromGoTypeName("set[natural]")));
			put("Int", PGoVariable.convert("Int", PGoType.inferFromGoTypeName("set[int]")));
			put("Real", PGoVariable.convert("Real", PGoType.inferFromGoTypeName("set[float64]")));
			put("Infinity", PGoVariable.convert("Infinity", PGoType.inferFromGoTypeName("float64")));
			put("STRING", PGoVariable.convert("STRING", PGoType.inferFromGoTypeName("set[string]")));
			put("BOOLEAN", PGoVariable.convert("BOOLEAN", PGoType.inferFromGoTypeName("set[bool]")));
		}
	};

	public PGoTempData(PGoTransIntermediateData data) {
		isMultiProcess = data.isMultiProcess;
		algName = data.algName;
		globals = new LinkedHashMap<>(data.globals);
		unresolvedVars = new LinkedHashMap<>(data.unresolvedVars);
		funcs = new LinkedHashMap<>(data.funcs);
		tlaExpr = data.tlaExpr;
		mainBlock = new Vector<>(data.mainBlock);
		goroutines = new LinkedHashMap<>(data.goroutines);
		annots = data.annots;
		needsLock = data.needsLock;
		defns = new LinkedHashMap<>(data.defns);
		tlaToAST = new HashMap<>(data.tlaToAST);
		labToLockGroup = new HashMap<>(data.labToLockGroup);
		numLockGroups = data.numLockGroups;
		locals = new LinkedHashMap<>();
	}

	// Clone the data passed in.
	public PGoTempData(PGoTempData data) {
		isMultiProcess = data.isMultiProcess;
		algName = data.algName;
		globals = new LinkedHashMap<>(data.globals);
		unresolvedVars = new LinkedHashMap<>(data.unresolvedVars);
		funcs = new LinkedHashMap<>(data.funcs);
		tlaExpr = data.tlaExpr;
		mainBlock = new Vector<>(data.mainBlock);
		goroutines = new LinkedHashMap<>(data.goroutines);
		annots = data.annots;
		needsLock = data.needsLock;
		defns = new LinkedHashMap<>(data.defns);
		tlaToAST = new HashMap<>(data.tlaToAST);
		labToLockGroup = new HashMap<>(data.labToLockGroup);
		numLockGroups = data.numLockGroups;
		locals = new LinkedHashMap<>(data.getLocals());
	}

	public Map<String, PGoVariable> getLocals() {
		return locals;
	}

	@Override
	public PGoVariable findPGoVariable(String name) {
		PGoVariable ret = super.findPGoVariable(name);
		if (ret == null || locals.containsKey(name)) {
			ret = locals.get(name);
		}
		if (ret == null || constants.containsKey(name)) {
			ret = constants.get(name);
		}
		return ret;
	}

	@Override
	public PGoFunction findPGoFunction(String name) {
		return super.findPGoFunction(name);
	}

	@Override
	public PGoTLADefinition findTLADefinition(String name) {
		return super.findTLADefinition(name);
	}

	public PGoLibFunction findBuiltInFunction(String name) {
		return super.findBuiltinFunction(name);
	}
}
