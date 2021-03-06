package pgo.trans.intermediate;

import pgo.errors.Issue;
import pgo.errors.IssueVisitor;
import pgo.model.pcal.MacroCall;

public class RecursiveMacroCallIssue extends Issue {

	private MacroCall macroCall;

	public RecursiveMacroCallIssue(MacroCall macroCall) {
		this.macroCall = macroCall;
	}
	
	public MacroCall getMacroCall() {
		return macroCall;
	}

	@Override
	public <T, E extends Throwable> T accept(IssueVisitor<T, E> v) throws E {
		return v.visit(this);
	}

}
