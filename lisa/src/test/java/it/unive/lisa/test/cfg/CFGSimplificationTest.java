package it.unive.lisa.test.cfg;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import it.unive.lisa.cfg.CFG;
import it.unive.lisa.cfg.CFGDescriptor;
import it.unive.lisa.cfg.edge.FalseEdge;
import it.unive.lisa.cfg.edge.SequentialEdge;
import it.unive.lisa.cfg.edge.TrueEdge;
import it.unive.lisa.cfg.statement.Assignment;
import it.unive.lisa.cfg.statement.Expression;
import it.unive.lisa.cfg.statement.Literal;
import it.unive.lisa.cfg.statement.NativeCall;
import it.unive.lisa.cfg.statement.NoOp;
import it.unive.lisa.cfg.statement.Return;
import it.unive.lisa.cfg.statement.Variable;

public class CFGSimplificationTest {

	@Test
	public void testSimpleSimplification() {
		CFG first = new CFG(new CFGDescriptor("foo"));
		Assignment assign = new Assignment(first, new Variable(first, "x"), new Literal(first, 5));
		NoOp noop = new NoOp(first);
		Return ret = new Return(first, new Variable(first, "x"));
		first.addNode(assign, true);
		first.addNode(noop);
		first.addNode(ret);
		first.addEdge(new SequentialEdge(assign, noop));
		first.addEdge(new SequentialEdge(noop, ret));
		
		
		CFG second = new CFG(new CFGDescriptor("foo"));
		assign = new Assignment(second, new Variable(second, "x"), new Literal(second, 5));
		ret = new Return(second, new Variable(second, "x"));

		second.addNode(assign, true);
		second.addNode(ret);
		
		second.addEdge(new SequentialEdge(assign, ret));

		first.simplify();
		assertTrue("Different CFGs", second.isEqualTo(first));
	}
	
	@Test
	public void testDoubleSimplification() {
		CFG first = new CFG(new CFGDescriptor("foo"));
		Assignment assign = new Assignment(first, new Variable(first, "x"), new Literal(first, 5));
		NoOp noop1 = new NoOp(first);
		NoOp noop2 = new NoOp(first);
		Return ret = new Return(first, new Variable(first, "x"));
		first.addNode(assign, true);
		first.addNode(noop1);
		first.addNode(noop2);
		first.addNode(ret);
		first.addEdge(new SequentialEdge(assign, noop1));
		first.addEdge(new SequentialEdge(noop1, noop2));
		first.addEdge(new SequentialEdge(noop2, ret));
		
		
		CFG second = new CFG(new CFGDescriptor("foo"));
		assign = new Assignment(second, new Variable(second, "x"), new Literal(second, 5));
		ret = new Return(second, new Variable(second, "x"));

		second.addNode(assign, true);
		second.addNode(ret);
		
		second.addEdge(new SequentialEdge(assign, ret));

		first.simplify();
		assertTrue("Different CFGs", second.isEqualTo(first));
	}
	
	@Test
	public void testConditionalSimplification() {
		class GT extends NativeCall {
			protected GT(CFG cfg, Expression left, Expression right) {
				super(cfg, "gt", left, right);
			}
		}
		
		class Print extends NativeCall {
			protected Print(CFG cfg, Expression arg) {
				super(cfg, "print", arg);
			}
		}
		
		CFG first = new CFG(new CFGDescriptor("foo"));
		Assignment assign = new Assignment(first, new Variable(first, "x"), new Literal(first, 5));
		GT gt = new GT(first, new Variable(first, "x"), new Literal(first, 2)); 
		Print print = new Print(first, new Literal(first, "f")); 
		NoOp noop1 = new NoOp(first);
		NoOp noop2 = new NoOp(first);
		Return ret = new Return(first, new Variable(first, "x"));
		first.addNode(assign, true);
		first.addNode(gt);
		first.addNode(print);
		first.addNode(noop1);
		first.addNode(noop2);
		first.addNode(ret);
		first.addEdge(new SequentialEdge(assign, gt));
		first.addEdge(new TrueEdge(gt, print));
		first.addEdge(new FalseEdge(gt, noop1));
		first.addEdge(new SequentialEdge(noop1, noop2));
		first.addEdge(new SequentialEdge(print, noop2));
		first.addEdge(new SequentialEdge(noop2, ret));
		
		
		CFG second = new CFG(new CFGDescriptor("foo"));
		assign = new Assignment(second, new Variable(second, "x"), new Literal(second, 5));
		gt = new GT(second, new Variable(second, "x"), new Literal(second, 2)); 
		print = new Print(second, new Literal(second, "f")); 
		ret = new Return(second, new Variable(second, "x"));

		second.addNode(assign, true);
		second.addNode(gt);
		second.addNode(print);
		second.addNode(ret);
		
		second.addEdge(new SequentialEdge(assign, gt));
		second.addEdge(new TrueEdge(gt, print));
		second.addEdge(new FalseEdge(gt, ret));
		second.addEdge(new SequentialEdge(print, ret));

		first.simplify();
		assertTrue("Different CFGs", second.isEqualTo(first));
	}
	
	@Test
	public void testSimplificationWithDuplicateStatements() {
		CFG first = new CFG(new CFGDescriptor("foo"));
		Assignment assign = new Assignment(first, new Variable(first, "x"), new Literal(first, 5));
		NoOp noop = new NoOp(first);
		Assignment ret = new Assignment(first, new Variable(first, "x"), new Literal(first, 5));
		first.addNode(assign);
		first.addNode(noop);
		first.addNode(ret);
		first.addEdge(new SequentialEdge(assign, noop));
		first.addEdge(new SequentialEdge(noop, ret));
		
		CFG second = new CFG(new CFGDescriptor("foo"));
		assign = new Assignment(second, new Variable(second, "x"), new Literal(second, 5));
		ret = new Assignment(first, new Variable(first, "x"), new Literal(first, 5));

		second.addNode(assign);
		second.addNode(ret);
		
		second.addEdge(new SequentialEdge(assign, ret));

		first.simplify();
		assertTrue("Different CFGs", second.isEqualTo(first));
	}
}