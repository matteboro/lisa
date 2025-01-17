package it.unive.lisa.analysis.string.tarsis;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticDomain;
import it.unive.lisa.analysis.SemanticDomain.Satisfiability;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.analysis.string.fsa.SimpleAutomaton;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.StringConcat;
import it.unive.lisa.symbolic.value.operator.binary.StringContains;
import it.unive.lisa.util.datastructures.automaton.CyclicAutomatonException;
import java.util.Objects;
import java.util.SortedSet;

/**
 * A class that represent the Tarsis domain for strings, exploiting a
 * {@link RegexAutomaton}.
 *
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 */
public class Tarsis implements BaseNonRelationalValueDomain<Tarsis> {

	/**
	 * Top element of the domain
	 */
	private static final Tarsis TOP = new Tarsis();

	/**
	 * Top element of the domain
	 */
	private static final Tarsis BOTTOM = new Tarsis(RegexAutomaton.emptyLang());

	/**
	 * Maximum widening threshold, or default threshold if there is no
	 * difference in the size of the two automata.
	 */
	public static final int WIDENING_CAP = 5;

	/**
	 * Used to store the string representation
	 */
	private final RegexAutomaton a;

	/**
	 * Creates a new FSA object representing the TOP element.
	 */
	public Tarsis() {
		// top
		this.a = RegexAutomaton.topString();
	}

	/**
	 * Creates a new FSA object using a {@link SimpleAutomaton}.
	 * 
	 * @param a the {@link SimpleAutomaton} used for object construction.
	 */
	Tarsis(RegexAutomaton a) {
		this.a = a;
	}

	@Override
	public Tarsis lubAux(Tarsis other) throws SemanticException {
		return new Tarsis(this.a.union(other.a).minimize());
	}

	@Override
	public Tarsis glbAux(Tarsis other) throws SemanticException {
		return new Tarsis(this.a.intersection(other.a).minimize());
	}

	/**
	 * Yields the size of this string, that is, the number of states of the
	 * underlying automaton.
	 * 
	 * @return the size of this string
	 */
	public int size() {
		return a.getStates().size();
	}

	private int getSizeDiffCapped(Tarsis other) {
		int size = size();
		int otherSize = other.size();
		if (size > otherSize)
			return Math.min(size - otherSize, WIDENING_CAP);
		else if (size < otherSize)
			return Math.min(otherSize - size, WIDENING_CAP);
		else
			return WIDENING_CAP;
	}

	@Override
	public Tarsis wideningAux(Tarsis other) throws SemanticException {
		return new Tarsis(this.a.union(other.a).widening(getSizeDiffCapped(other)));
	}

	@Override
	public boolean lessOrEqualAux(Tarsis other) throws SemanticException {
		return this.a.isContained(other.a);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Tarsis fsa = (Tarsis) o;
		return Objects.equals(a, fsa.a);
	}

	@Override
	public int hashCode() {
		return Objects.hash(a);
	}

	@Override
	public Tarsis top() {
		return TOP;
	}

	@Override
	public Tarsis bottom() {
		return BOTTOM;
	}

	@Override
	public DomainRepresentation representation() {
		if (isBottom())
			return Lattice.bottomRepresentation();
		else if (isTop())
			return Lattice.topRepresentation();

		return new StringRepresentation(this.a.toRegex());
	}

	@Override
	public Tarsis evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
		if (constant.getValue() instanceof String)
			return new Tarsis(RegexAutomaton.string((String) constant.getValue()));
		return top();
	}

	// TODO unary and ternary and all other binary
	@Override
	public Tarsis evalBinaryExpression(BinaryOperator operator, Tarsis left, Tarsis right, ProgramPoint pp)
			throws SemanticException {
		if (operator == StringConcat.INSTANCE)
			return new Tarsis(left.a.concat(right.a));
		return top();
	}

	@Override
	public Satisfiability satisfiesBinaryExpression(BinaryOperator operator, Tarsis left, Tarsis right,
			ProgramPoint pp) throws SemanticException {
		if (operator == StringContains.INSTANCE)
			return left.contains(right);
		return SemanticDomain.Satisfiability.UNKNOWN;
	}

	/**
	 * Semantics of {@link StringContains} between {@code this} and
	 * {@code other}.
	 * 
	 * @param other the other domain instance
	 * 
	 * @return the satisfiability result
	 */
	public Satisfiability contains(Tarsis other) {
		try {
			if (!a.hasCycle()
					&& !other.a.hasCycle()
					&& !a.acceptsTopEventually()
					&& !other.a.acceptsTopEventually()) {
				// we can compare languages
				boolean atLeastOne = false, all = true;
				for (String a : a.getLanguage())
					for (String b : other.a.getLanguage()) {
						boolean cont = a.contains(b);
						atLeastOne = atLeastOne || cont;
						all = all && cont;
					}

				if (all)
					return Satisfiability.SATISFIED;
				if (atLeastOne)
					return Satisfiability.UNKNOWN;
				return Satisfiability.NOT_SATISFIED;
			}

			if (!other.a.hasCycle() && other.a.getLanguage().size() == 1
					&& other.a.getLanguage().iterator().next().isEmpty())
				// the empty string is always contained
				return Satisfiability.SATISFIED;

			if (other.a.hasOnlyOnePath()) {
				RegexAutomaton C = other.a.extractLongestString();
				String longest = C.getLanguage().iterator().next();
				RegexAutomaton withNoScc = a.minimize().makeAcyclic();
				boolean all = true;
				SortedSet<String> lang = withNoScc.getLanguage();
				for (String a : lang)
					all = all && a.contains(longest);

				if (!lang.isEmpty() && all)
					return Satisfiability.SATISFIED;
			}

			RegexAutomaton transformed = a.explode().factors();
			RegexAutomaton otherExploded = other.a.explode();
			if (otherExploded.intersection(transformed).acceptsEmptyLanguage())
				// we can explode since it does not matter how the inner strings
				// overlap
				return Satisfiability.NOT_SATISFIED;

		} catch (CyclicAutomatonException e) {
			// can safely ignore
		}
		return Satisfiability.UNKNOWN;
	}
}
