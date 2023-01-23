package it.unive.lisa.interprocedural;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import it.unive.lisa.LiSAConfiguration.DescendingPhaseType;
import it.unive.lisa.analysis.AbstractState;
import it.unive.lisa.analysis.AnalysisState;
import it.unive.lisa.analysis.CFGWithAnalysisResults;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.StatementStore;
import it.unive.lisa.analysis.decouple.CFGWithAnalysisResultsDecoupler;
import it.unive.lisa.analysis.heap.HeapDomain;
import it.unive.lisa.analysis.lattices.ExpressionSet;
import it.unive.lisa.analysis.symbols.SymbolAliasing;
import it.unive.lisa.analysis.value.TypeDomain;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.interprocedural.callgraph.CallGraph;
import it.unive.lisa.interprocedural.callgraph.CallResolutionException;
import it.unive.lisa.program.Application;
import it.unive.lisa.program.SyntheticLocation;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.call.CFGCall;
import it.unive.lisa.program.cfg.statement.call.Call;
import it.unive.lisa.program.cfg.statement.call.OpenCall;
import it.unive.lisa.program.cfg.statement.call.UnresolvedCall;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.Skip;
import it.unive.lisa.type.Type;
import it.unive.lisa.util.collections.workset.WorkingSet;
import it.unive.lisa.util.datastructures.graph.algorithms.FixpointException;

public class DecouplingModularWorstCaseAnalysisInterprocedural<
	AA extends AbstractState<AA,HA,VA,TA>, 
	HA extends HeapDomain<HA>, 
	VA extends ValueDomain<VA>, 
	TA extends TypeDomain<TA>,
	AD extends AbstractState<AD,HD,VD,TD>, 
	HD extends HeapDomain<HD>, 
	VD extends ValueDomain<VD>, 
	TD extends TypeDomain<TD>> implements 
	InterproceduralAnalysis<AD, HD, VD, TD> {
	
	public ModularWorstCaseAnalysis<AA, HA, VA, TA> ascendingInterproc;
	public ModularWorstCaseAnalysis<AD, HD, VD, TD> descendingInterproc;
	public CFGWithAnalysisResultsDecoupler<AA, HA, VA, TA, AD, HD, VD, TD> decoupler; 
	
	public AA ascendingState;
	public AD descendingState;
	
	private Application app;
	
	Map<CFG, Optional<CFGWithAnalysisResults<AD, HD, VD, TD>>> results;
	
	public DecouplingModularWorstCaseAnalysisInterprocedural(
									CFGWithAnalysisResultsDecoupler<AA, HA, VA, TA, AD, HD, VD, TD> decoupler, AA ascendingState, AD descendingState){
		this.ascendingInterproc = new ModularWorstCaseAnalysis<AA, HA, VA, TA>();
		this.descendingInterproc = new ModularWorstCaseAnalysis<AD, HD, VD, TD>();
		this.decoupler = decoupler;
		this.decoupler.setStates(ascendingState, descendingState);
		this.ascendingState = ascendingState;
		this.descendingState = descendingState;
	}
	
	@Override
	public void init(Application app, CallGraph callgraph, OpenCallPolicy policy)
			throws InterproceduralAnalysisException {
		ascendingInterproc.init(app, callgraph, policy);
		descendingInterproc.init(app, callgraph, policy);
		this.app = app;
	}
	
	@Override
	public void fixpoint(AnalysisState<AD, HD, VD, TD> entryState,
			Class<? extends WorkingSet<Statement>> fixpointWorkingSet, int wideningThreshold,
			DescendingPhaseType descendingPhase, int descendingGlbThreshold) throws FixpointException {
		
		ascendingInterproc.fixpoint(
				new AnalysisState<>(this.ascendingState.bottom(), 
				new Skip(SyntheticLocation.INSTANCE), 
				new SymbolAliasing()), 
					fixpointWorkingSet, 
					wideningThreshold, 
					DescendingPhaseType.NONE, 
					0);
		
		 Map<CFG, Optional<CFGWithAnalysisResults<AA, HA, VA, TA>>> ascendingResults = ascendingInterproc.getResults();
		 Map<CFG, Optional<CFGWithAnalysisResults<AD, HD, VD, TD>>> descendingStartingResults = new HashMap<>();
		 
		 for(CFG cfg : app.getAllCFGs()) {
			 if(ascendingResults.get(cfg).isPresent()) {
				 descendingStartingResults.put(cfg, Optional.of(this.decoupler.decouple(ascendingResults.get(cfg).get())));
			 }
			 else
				 descendingStartingResults.put(cfg, Optional.of(null));
		 }
		 
		 descendingInterproc.descendingPhase(
				new AnalysisState<>(this.descendingState.bottom(), 
				new Skip(SyntheticLocation.INSTANCE), 
				new SymbolAliasing()), 
					fixpointWorkingSet, 
					descendingPhase, 
					descendingGlbThreshold, 
					descendingStartingResults);
		 
		 this.results = descendingInterproc.getResults();
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
