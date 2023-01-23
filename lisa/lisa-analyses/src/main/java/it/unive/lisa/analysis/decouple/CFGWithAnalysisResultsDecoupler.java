package it.unive.lisa.analysis.decouple;

import it.unive.lisa.analysis.AbstractState;
import it.unive.lisa.analysis.CFGWithAnalysisResults;
import it.unive.lisa.analysis.heap.HeapDomain;
import it.unive.lisa.analysis.value.TypeDomain;
import it.unive.lisa.analysis.value.ValueDomain;

public interface CFGWithAnalysisResultsDecoupler <
	AA extends AbstractState<AA,HA,VA,TA>, 
	HA extends HeapDomain<HA>, 
	VA extends ValueDomain<VA>, 
	TA extends TypeDomain<TA>,
	AD extends AbstractState<AD,HD,VD,TD>, 
	HD extends HeapDomain<HD>, 
	VD extends ValueDomain<VD>, 
	TD extends TypeDomain<TD>>{
	
	public CFGWithAnalysisResults<AD,HD,VD,TD> decouple(CFGWithAnalysisResults<AA,HA,VA,TA> ascendingResult);
}
