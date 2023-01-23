package it.unive.lisa.cron.decouple;

import static it.unive.lisa.LiSAFactory.getDefaultFor;

import org.junit.Test;

import it.unive.lisa.AnalysisSetupException;
import it.unive.lisa.AnalysisTestExecutor;
import it.unive.lisa.LiSAConfiguration;
import it.unive.lisa.LiSAConfiguration.GraphType;
import it.unive.lisa.analysis.AbstractState;
import it.unive.lisa.analysis.decouple.SignToIntervalDecoupler;
import it.unive.lisa.analysis.heap.HeapDomain;
import it.unive.lisa.analysis.nonrelational.value.TypeEnvironment;
import it.unive.lisa.analysis.numeric.Sign;
import it.unive.lisa.analysis.types.InferredTypes;
import it.unive.lisa.interprocedural.DecouplingModularWorstCaseAnalysisInterprocedural;

public class DecouplingTest extends AnalysisTestExecutor {

	@Test
	public void testSigntoInterval() throws AnalysisSetupException {
		LiSAConfiguration conf = new LiSAConfiguration();
		conf.serializeResults = true;
		
		@SuppressWarnings({ "rawtypes" })
		SignToIntervalDecoupler decoupler = new SignToIntervalDecoupler<>(getDefaultFor(HeapDomain.class), new TypeEnvironment<>(new InferredTypes()));
		@SuppressWarnings({ "unchecked", "rawtypes" })
		DecouplingModularWorstCaseAnalysisInterprocedural interproc = new DecouplingModularWorstCaseAnalysisInterprocedural<>(decoupler);
		
		conf.interproceduralAnalysis = interproc;
		conf.abstractState = getDefaultFor(AbstractState.class, getDefaultFor(HeapDomain.class), new Sign(),
				new TypeEnvironment<>(new InferredTypes()));
		conf.analysisGraphs = GraphType.DOT;
		perform("decoupling-sign-to-interval", "program.imp", conf);
	}
}