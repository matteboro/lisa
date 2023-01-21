package it.unive.lisa.interprocedural;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import it.unive.lisa.LiSAConfiguration.DescendingPhaseType;
import it.unive.lisa.analysis.AbstractState;
import it.unive.lisa.analysis.AnalysisState;
import it.unive.lisa.analysis.CFGWithAnalysisResults;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.StatementStore;
import it.unive.lisa.analysis.heap.HeapDomain;
import it.unive.lisa.analysis.lattices.ExpressionSet;
import it.unive.lisa.analysis.symbols.SymbolAliasing;
import it.unive.lisa.analysis.value.TypeDomain;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.interprocedural.callgraph.CallGraph;
import it.unive.lisa.interprocedural.callgraph.CallResolutionException;
import it.unive.lisa.program.Application;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.call.CFGCall;
import it.unive.lisa.program.cfg.statement.call.Call;
import it.unive.lisa.program.cfg.statement.call.OpenCall;
import it.unive.lisa.program.cfg.statement.call.UnresolvedCall;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.type.Type;
import it.unive.lisa.util.collections.workset.WorkingSet;
import it.unive.lisa.util.datastructures.graph.algorithms.FixpointException;

public class DecouplingInterprocedural<
	AA extends AbstractState<AA,HA,VA,TA>, 
	HA extends HeapDomain<HA>, 
	VA extends ValueDomain<VA>, 
	TA extends TypeDomain<TA>,
	AD extends AbstractState<AD,HD,VD,TD>, 
	HD extends HeapDomain<HD>, 
	VD extends ValueDomain<VD>, 
	TD extends TypeDomain<TD>> implements 
	InterproceduralAnalysis<AD, HD, VD, TD> {
	
	InterproceduralAnalysis<AA, HA, VA, TA> ascendingInterproc;
	InterproceduralAnalysis<AD, HD, VD, TD> descendingInterproc;
	
	@Override
	public void init(Application app, CallGraph callgraph, OpenCallPolicy policy)
			throws InterproceduralAnalysisException {
		ascendingInterproc.init(app, callgraph, policy);
		descendingInterproc.init(app, callgraph, policy);
	}
	
	@Override
	public void fixpoint(AnalysisState<AD, HD, VD, TD> entryState,
			Class<? extends WorkingSet<Statement>> fixpointWorkingSet, int wideningThreshold,
			DescendingPhaseType descendingPhase, int descendingGlbThreshold) throws FixpointException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<CFGWithAnalysisResults<AD, HD, VD, TD>> getAnalysisResultsOf(CFG cfg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AnalysisState<AD, HD, VD, TD> getAbstractResultOf(CFGCall call, AnalysisState<AD, HD, VD, TD> entryState,
			ExpressionSet<SymbolicExpression>[] parameters, StatementStore<AD, HD, VD, TD> expressions)
			throws SemanticException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AnalysisState<AD, HD, VD, TD> getAbstractResultOf(OpenCall call, AnalysisState<AD, HD, VD, TD> entryState,
			ExpressionSet<SymbolicExpression>[] parameters, StatementStore<AD, HD, VD, TD> expressions)
			throws SemanticException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Call resolve(UnresolvedCall call, Set<Type>[] types, SymbolAliasing aliasing)
			throws CallResolutionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void descendingPhase(AnalysisState<AD, HD, VD, TD> entryState,
			Class<? extends WorkingSet<Statement>> fixpointWorkingSet, DescendingPhaseType descendingPhase,
			int descendingGlbThreshold, Map<CFG, Optional<CFGWithAnalysisResults<AD, HD, VD, TD>>> ascendingResult)
			throws FixpointException {
		// TODO Auto-generated method stub
		
	}
}
