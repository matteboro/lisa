package it.unive.lisa.analysis.decouple;

import it.unive.lisa.analysis.AnalysisState;
import it.unive.lisa.analysis.CFGWithAnalysisResults;
import it.unive.lisa.analysis.SimpleAbstractState;
import it.unive.lisa.analysis.StatementStore;
import it.unive.lisa.analysis.heap.HeapDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.analysis.numeric.Sign;
import it.unive.lisa.analysis.symbols.SymbolAliasing;
import it.unive.lisa.analysis.value.TypeDomain;
import it.unive.lisa.program.SyntheticLocation;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.Skip;
import it.unive.lisa.util.numeric.IntInterval;
import it.unive.lisa.util.numeric.MathNumber;

public class SignToIntervalDecoupler<
H extends HeapDomain<H>, 
T extends TypeDomain<T>> 
	implements CFGWithAnalysisResultsDecoupler<
					SimpleAbstractState<H, ValueEnvironment<Sign>, T>, 
					H, 
					ValueEnvironment<Sign>, 
					T, 
					SimpleAbstractState<H, ValueEnvironment<Interval>, T>, 
					H, 
					ValueEnvironment<Interval>, 
					T>{

	private SimpleAbstractState<H, ValueEnvironment<Interval>, T> descendingState;
	private SimpleAbstractState<H, ValueEnvironment<Sign>, T> ascendingState;
	public SignToIntervalDecoupler(H heap, T type) {
		
	}
	
	private Interval apply(Sign sign) {
		if(sign == Sign.BOTTOM)
			return Interval.BOTTOM;
		else if(sign == Sign.TOP)
			return Interval.TOP;
		else if(sign == Sign.ZERO)
			return Interval.ZERO;
		else if(sign == Sign.POS)	
			return new Interval(new IntInterval(MathNumber.ONE, MathNumber.PLUS_INFINITY));
		else if(sign == Sign.NEG)	
			return new Interval(new IntInterval(MathNumber.MINUS_INFINITY, MathNumber.MINUS_ONE));
		else
			return Interval.TOP;
		
	}
	
	private ValueEnvironment<Interval> decoupleEnvironment(ValueEnvironment<Sign> signEnv){
		
		ValueEnvironment<Interval> intervalEnv = new ValueEnvironment<>(Interval.BOTTOM);
		
		if(signEnv.isBottom())
			return intervalEnv.bottom();
		if(signEnv.function == null) {
			return intervalEnv.bottom();
		}
		
		for(Identifier id : signEnv.function.keySet()) {
			intervalEnv = intervalEnv.putState(id,apply(signEnv.getState(id)));
		}
		
		return intervalEnv;
	}
	
	private StatementStore<SimpleAbstractState<H, ValueEnvironment<Interval>, T>, H, ValueEnvironment<Interval>, T> decoupleStatementStore(
			StatementStore<SimpleAbstractState<H, ValueEnvironment<Sign>, T>, H, ValueEnvironment<Sign>, T> ascendingResult){
		
		StatementStore<SimpleAbstractState<H, ValueEnvironment<Interval>, T>, H, ValueEnvironment<Interval>, T> decoupledStatementStore = 
				new StatementStore<>(new AnalysisState<>(this.descendingState.bottom(), new Skip(SyntheticLocation.INSTANCE), new SymbolAliasing()).bottom());
		
		ascendingResult.function.forEach((st, state) -> decoupledStatementStore.put(st, new AnalysisState<>(
																				new SimpleAbstractState<>(
																							state.getState().getHeapState(), 
																							decoupleEnvironment(state.getState().getValueState()),
																							state.getState().getTypeState()), 
																				state.getComputedExpressions(), 
																				state.getAliasing())));
		
		return decoupledStatementStore;
	}
	
	@Override
	public CFGWithAnalysisResults<SimpleAbstractState<H, ValueEnvironment<Interval>, T>, H, ValueEnvironment<Interval>, T> decouple(
			CFGWithAnalysisResults<SimpleAbstractState<H, ValueEnvironment<Sign>, T>, H, ValueEnvironment<Sign>, T> ascendingResult) {
		
		CFGWithAnalysisResults<SimpleAbstractState<H, ValueEnvironment<Interval>, T>, H, ValueEnvironment<Interval>, T> decoupledResults = 
				new CFGWithAnalysisResults<>(ascendingResult, decoupleStatementStore(ascendingResult.getEntryStateStore()), decoupleStatementStore(ascendingResult.getResults()));
		
		return decoupledResults;
	}

	@Override
	public void setStates(SimpleAbstractState<H, ValueEnvironment<Sign>, T> ascendingState,
			SimpleAbstractState<H, ValueEnvironment<Interval>, T> descendingState) {
		this.descendingState = descendingState;
		this.ascendingState = ascendingState;
	}

	@Override
	public SimpleAbstractState<H, ValueEnvironment<Sign>, T> getAscendingState() {
		return this.ascendingState;
	}

	@Override
	public SimpleAbstractState<H, ValueEnvironment<Interval>, T> getDescendingState() {
		return this.descendingState;
	}
}
