package it.unive.lisa.analysis.impl.heap.pointbased;

import java.util.HashSet;
import java.util.Set;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.lattices.ExpressionSet;
import it.unive.lisa.analysis.nonrelational.heap.HeapEnvironment;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.heap.AccessChild;
import it.unive.lisa.symbolic.heap.HeapAllocation;
import it.unive.lisa.symbolic.value.MemoryPointer;
import it.unive.lisa.symbolic.value.ValueExpression;

/**
 * A field-sensitive point-based heap implementation that abstracts heap
 * locations depending on their allocation sites, namely the position of the
 * code where heap locations are generated. All heap locations that are
 * generated at the same allocation sites are abstracted into a single unique
 * heap identifier. The analysis is field-sensitive in the sense that all the
 * field accesses, with the same field, to a specific allocation site are
 * abstracted into a single heap identifier. The implementation follows X. Rival
 * and K. Yi, "Introduction to Static Analysis An Abstract Interpretation
 * Perspective", Section 8.3.4
 * 
 * @author <a href="mailto:vincenzo.arceri@unive.it">Vincenzo Arceri</a>
 * 
 * @see <a href=
 *          "https://mitpress.mit.edu/books/introduction-static-analysis">https://mitpress.mit.edu/books/introduction-static-analysis</a>
 */
public class FieldSensitivePointBasedHeap extends PointBasedHeap {

	/**
	 * Builds a new instance of field-sensitive point-based heap.
	 */
	public FieldSensitivePointBasedHeap() {
		super();
	}

	private FieldSensitivePointBasedHeap(HeapEnvironment<AllocationSites> allocationSites) {
		super(allocationSites);
	}

	@Override
	protected FieldSensitivePointBasedHeap from(PointBasedHeap original) {
		return new FieldSensitivePointBasedHeap(original.heapEnv);
	}
	
	private AllocationSite alreadyAllocated(AllocationSite id) {
		for (AllocationSites set : heapEnv.values())
			for (AllocationSite site : set)
				if (site.getName().equals(id.getName()))
					return site;

		return null;
	}
	
	@Override
	public ExpressionSet<ValueExpression> rewrite(SymbolicExpression expression, ProgramPoint pp)
			throws SemanticException {
		return expression.accept(new Rewriter(), pp);
	}

	private class Rewriter extends PointBasedHeap.Rewriter {
		
		@Override
		public ExpressionSet<ValueExpression> visit(AccessChild expression, ExpressionSet<ValueExpression>  receiver,
				ExpressionSet<ValueExpression> child, Object... params) throws SemanticException {
			AccessChild access = (AccessChild) expression;
			Set<ValueExpression> result = new HashSet<>();

			for (SymbolicExpression contRewritten : receiver)
				if (contRewritten instanceof MemoryPointer) {
					AllocationSite site = (AllocationSite) ((MemoryPointer) contRewritten).getLocation();
					for (SymbolicExpression childRewritten : child)
						result.add(new AllocationSite(access.getTypes(), site.getId(), childRewritten, site.isWeak()));
				}

			return new ExpressionSet<>(result);
		}
		
		@Override
		public ExpressionSet<ValueExpression> visit(HeapAllocation expression, Object... params)
				throws SemanticException {
			AllocationSite id = new AllocationSite(expression.getTypes(),
					((ProgramPoint) params[0]).getLocation().getCodeLocation());

			if (alreadyAllocated(id) != null) 
				return new ExpressionSet<>(new AllocationSite(id.getTypes(), id.getId(), true));
			else
				return new ExpressionSet<>(new AllocationSite(id.getTypes(), id.getId(), false));
		}
	}
}