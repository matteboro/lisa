package it.unive.lisa.program.cfg.statement.string;

import it.unive.lisa.analysis.AbstractState;
import it.unive.lisa.analysis.AnalysisState;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.heap.HeapDomain;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.caches.Caches;
import it.unive.lisa.interprocedural.InterproceduralAnalysis;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.CodeLocation;
import it.unive.lisa.program.cfg.NativeCFG;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.PluggableStatement;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.call.UnaryNativeCall;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.UnaryOperator;
import it.unive.lisa.type.common.Int32;
import it.unive.lisa.type.common.StringType;

/**
 * An expression modeling the string length operation. The type of the operand
 * must be {@link StringType}. The type of this expression is the {@link Int32}.
 * <br>
 * <br>
 * Since in most languages string operations are provided through calls to
 * library functions, this class contains a field {@link #originating} whose
 * purpose is to optionally store a {@link Statement} that is rewritten to an
 * instance of this class (i.e., a call to a {@link NativeCFG} modeling the
 * library function). If present, such statement will be used as
 * {@link ProgramPoint} for semantics computations. This allows subclasses to
 * implement {@link PluggableStatement} easily without redefining the semantics
 * provided by this class.
 * 
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 */
public class Length extends UnaryNativeCall {

	/**
	 * Statement that has been rewritten to this operation, if any. This is to
	 * accomodate the fact that, in most languages, string operations are
	 * performed through calls, and one might want to provide the semantics of
	 * those calls through {@link NativeCFG} that rewrites to instances of this
	 * class.
	 */
	protected Statement originating;

	/**
	 * Builds the length.
	 * 
	 * @param cfg       the {@link CFG} where this operation lies
	 * @param location  the code location where this operation is defined
	 * @param parameter the operand of this operation
	 */
	public Length(CFG cfg, CodeLocation location, Expression parameter) {
		super(cfg, location, "len", Int32.INSTANCE, parameter);
	}

	@Override
	protected <A extends AbstractState<A, H, V>,
			H extends HeapDomain<H>,
			V extends ValueDomain<V>> AnalysisState<A, H, V> unarySemantics(AnalysisState<A, H, V> entryState,
					InterproceduralAnalysis<A, H, V> interprocedural, AnalysisState<A, H, V> exprState,
					SymbolicExpression expr)
					throws SemanticException {
		// we allow untyped for the type inference phase
		if (!expr.getDynamicType().isStringType() && !expr.getDynamicType().isUntyped())
			return entryState.bottom();

		return exprState.smallStepSemantics(
				new UnaryExpression(
						Caches.types().mkSingletonSet(Int32.INSTANCE),
						expr,
						UnaryOperator.STRING_LENGTH,
						getLocation()),
				originating == null ? this : originating);
	}

}