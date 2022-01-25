package it.unive.lisa.program.cfg.statement.call;

import it.unive.lisa.interprocedural.callgraph.CallGraph;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.CodeLocation;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.NaryExpression;
import it.unive.lisa.program.cfg.statement.call.assignment.ParameterAssigningStrategy;
import it.unive.lisa.program.cfg.statement.evaluation.EvaluationOrder;
import it.unive.lisa.program.cfg.statement.evaluation.LeftToRightEvaluation;
import it.unive.lisa.type.Type;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * A call to another cfg.
 * 
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 */
public abstract class Call extends NaryExpression {

	/**
	 * The original {@link UnresolvedCall} that has been resolved to this one
	 */
	private UnresolvedCall source = null;

	/**
	 * The {@link ParameterAssigningStrategy} of the parameters of this call
	 */
	private final ParameterAssigningStrategy assigningStrategy;

	/**
	 * An optional qualifier for the call.
	 */
	private final String qualifier;

	/**
	 * The name of the target of the call.
	 */
	private final String targetName;

	/**
	 * Whether or not this is a call to an instance method of a unit (that can
	 * be overridden) or not.
	 */
	private final boolean instanceCall;

	/**
	 * Builds a call happening at the given source location. The
	 * {@link EvaluationOrder} of the parameter is
	 * {@link LeftToRightEvaluation}.
	 * 
	 * @param cfg               the cfg that this expression belongs to
	 * @param location          the location where the expression is defined
	 *                              within the program
	 * @param assigningStrategy the {@link ParameterAssigningStrategy} of the
	 *                              parameters of this call
	 * @param instanceCall      whether or not this is a call to an instance
	 *                              method of a unit (that can be overridden) or
	 *                              not
	 * @param qualifier         the optional qualifier of the call (can be null
	 *                              or empty - see {@link #getFullTargetName()}
	 *                              for more info)
	 * @param targetName        the name of the target of this call
	 * @param staticType        the static type of this call
	 * @param parameters        the parameters of this call
	 */
	protected Call(CFG cfg, CodeLocation location, ParameterAssigningStrategy assigningStrategy,
			boolean instanceCall, String qualifier, String targetName, Type staticType,
			Expression... parameters) {
		this(cfg, location, assigningStrategy, instanceCall, qualifier, targetName, LeftToRightEvaluation.INSTANCE,
				staticType, parameters);
	}

	/**
	 * Builds a call happening at the given source location.
	 * 
	 * @param cfg               the cfg that this expression belongs to
	 * @param location          the location where the expression is defined
	 *                              within the program
	 * @param assigningStrategy the {@link ParameterAssigningStrategy} of the
	 *                              parameters of this call
	 * @param instanceCall      whether or not this is a call to an instance
	 *                              method of a unit (that can be overridden) or
	 *                              not
	 * @param qualifier         the optional qualifier of the call (can be null
	 *                              or empty - see {@link #getFullTargetName()}
	 *                              for more info)
	 * @param targetName        the name of the target of this call
	 * @param order             the evaluation order of the sub-expressions
	 * @param staticType        the static type of this call
	 * @param parameters        the parameters of this call
	 */
	protected Call(CFG cfg, CodeLocation location, ParameterAssigningStrategy assigningStrategy, boolean instanceCall,
			String qualifier, String targetName, EvaluationOrder order, Type staticType, Expression... parameters) {
		super(cfg, location, completeName(qualifier, targetName), order, staticType, parameters);
		Objects.requireNonNull(targetName, "The name of the target of a call cannot be null");
		Objects.requireNonNull(assigningStrategy, "The assigning strategy of a call cannot be null");
		this.targetName = targetName;
		this.qualifier = qualifier;
		this.instanceCall = instanceCall;
		this.assigningStrategy = assigningStrategy;
	}

	/**
	 * Yields the {@link ParameterAssigningStrategy} of the parameters of this
	 * call.
	 * 
	 * @return the assigning strategy
	 */
	public ParameterAssigningStrategy getAssigningStrategy() {
		return assigningStrategy;
	}

	private static String completeName(String qualifier, String name) {
		return StringUtils.isNotBlank(qualifier) ? qualifier + "::" + name : name;
	}

	/**
	 * Yields the call that this call originated from, if any. A call <i>r</i>
	 * originates from a call <i>u</i> if:
	 * <ul>
	 * <li><i>u</i> is an {@link UnresolvedCall}, while <i>r</i> is not,
	 * and</li>
	 * <li>a {@link CallGraph} resolved <i>u</i> to <i>r</i>, or</li>
	 * <li>a {@link CallGraph} resolved <i>u</i> to a call <i>c</i> (e.g. a
	 * {@link HybridCall}), and its semantics generated the call <i>u</i></li>
	 * </ul>
	 * 
	 * @return the call that this one originated from
	 */
	public final UnresolvedCall getSource() {
		return source;
	}

	/**
	 * Yields the full name of the target of the call. The full name of the
	 * target of a call follows the following structure:
	 * {@code qualifier::targetName}, where {@code qualifier} is optional and,
	 * when it is not present (i.e. null or empty), the {@code ::} are omitted.
	 * This method returns is an alias of {@link #getConstructName()}.
	 * 
	 * @return the full name of the target of the call
	 */
	public String getFullTargetName() {
		return getConstructName();
	}

	/**
	 * Yields the name of the target of the call. The full name of the target of
	 * a call follows the following structure: {@code qualifier::targetName},
	 * where {@code qualifier} is optional and, when it is not present (i.e.
	 * null or empty), the {@code ::} are omitted.
	 * 
	 * @return the name of the target of the call
	 */
	public String getTargetName() {
		return targetName;
	}

	/**
	 * Yields the optional qualifier of the target of the call. The full name of
	 * the target of a call follows the following structure:
	 * {@code qualifier::targetName}, where {@code qualifier} is optional and,
	 * when it is not present (i.e. null or empty), the {@code ::} are omitted.
	 * 
	 * @return the qualifier of the target of the call
	 */
	public String getQualifier() {
		return qualifier;
	}

	/**
	 * Yields the parameters of this call. This is a shortcut to invoke
	 * {@link #getSubExpressions()}.
	 * 
	 * @return the parameters of this call
	 */
	public final Expression[] getParameters() {
		return getSubExpressions();
	}

	/**
	 * Yields whether or not this is a call to an instance method of a unit
	 * (that can be overridden) or not.
	 * 
	 * @return {@code true} if this call targets instance cfgs, {@code false}
	 *             otherwise
	 */
	public boolean isInstanceCall() {
		return instanceCall;
	}

	/**
	 * Sets the call that this call originated from. A call <i>r</i> originates
	 * from a call <i>u</i> if:
	 * <ul>
	 * <li><i>u</i> is an {@link UnresolvedCall}, while <i>r</i> is not,
	 * and</li>
	 * <li>a {@link CallGraph} resolved <i>u</i> to <i>r</i>, or</li>
	 * <li>a {@link CallGraph} resolved <i>u</i> to a call <i>c</i> (e.g. a
	 * {@link HybridCall}), and its semantics generated the call <i>u</i></li>
	 * </ul>
	 * 
	 * @param source the call that this one originated from
	 */
	public final void setSource(UnresolvedCall source) {
		this.source = source;
	}

	@Override
	public String toString() {
		return getConstructName() + "(" + StringUtils.join(getParameters(), ", ") + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (instanceCall ? 1231 : 1237);
		result = prime * result + ((qualifier == null) ? 0 : qualifier.hashCode());
		result = prime * result + ((targetName == null) ? 0 : targetName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Call))
			return false;
		Call other = (Call) obj;
		if (instanceCall != other.instanceCall)
			return false;
		if (qualifier == null) {
			if (other.qualifier != null)
				return false;
		} else if (!qualifier.equals(other.qualifier))
			return false;
		if (targetName == null) {
			if (other.targetName != null)
				return false;
		} else if (!targetName.equals(other.targetName))
			return false;
		return true;
	}
}
