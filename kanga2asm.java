import syntaxtree.*;
import visitor.GJNoArguDepthFirst;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


public class kanga2asm extends GJNoArguDepthFirst<String> {
	private MipsBuffer b;
	private int arguments;
	private int spillCount;
	private int maxCount;
	private int passargCount = 0;

	private static final Map<String,String> op2asm = new HashMap<String, String>(){{
		put("LT","slt");
		put("PLUS","add");
		put("MINUS","sub");
		put("TIMES","mul");
	}};

	public static final class MipsBuffer {
		StringBuilder s = new StringBuilder();

		public <T> void append(T ... args){
			for(T arg : args)
				s.append(arg).append(' ');
			s.append('\n');
		}

		@Override
		public String toString() {
			return s.toString();
		}
	}

	public kanga2asm() {
		this.b = new MipsBuffer();
	}

	/**
	 * Represents a grammar list, e.g. ( A )+
	 */
	@Override
	public String visit(NodeList n) throws Exception {
		for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); )
			e.nextElement().accept(this);
		return null;
	}

	/**
	 * Represents an optional grammar list, e.g. ( A )*
	 */
	@Override
	public String visit(NodeListOptional n) throws Exception {
		for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); )
			e.nextElement().accept(this);
		return null;
	}

	/**
	 * Represents an grammar optional node, e.g. ( A )? or [ A ]
	 */
	@Override
	public String visit(NodeOptional n) throws Exception {
		if(n.present() && n.node instanceof Label)
			b.append(n.node.accept(this)+":");
		return n.present() ? n.node.accept(this) : null;
	}

	/**
	 * Represents a sequence of nodes nested within a choice, list,
	 * optional list, or optional, e.g. ( A B )+ or [ C D E ]
	 */
	@Override
	public String visit(NodeSequence n) throws Exception {
		for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); )
			e.nextElement().accept(this);
		return null;
	}

	/**
	 * Represents a single token in the grammar.  If the "-tk" option
	 * is used, also contains a Vector of preceding special tokens.
	 */
	@Override
	public String visit(NodeToken n) throws Exception {
		return n.tokenImage;
	}

	/**
	 * Grammar production:
	 * f0 -> "MAIN"
	 * f1 -> "["
	 * f2 -> IntegerLiteral()
	 * f3 -> "]"
	 * f4 -> "["
	 * f5 -> IntegerLiteral()
	 * f6 -> "]"
	 * f7 -> "["
	 * f8 -> IntegerLiteral()
	 * f9 -> "]"
	 * f10 -> StmtList()
	 * f11 -> "END"
	 * f12 -> ( Procedure() )*
	 * f13 -> <EOF>
	 */
	@Override
	public String visit(Goal n) throws Exception {
		arguments = Integer.parseInt(n.f2.f0.tokenImage);
		spillCount = Integer.parseInt(n.f5.f0.tokenImage);
		maxCount = Integer.parseInt(n.f8.f0.tokenImage);
		int stackOffset;
		b.append(".data");
		b.append("errormsg:",".asciiz","\"\\033[34m(mission) aborted.\\033[0m " +
				"\\033[5;31mabandon ship!\\033[0m " +
				"\\033[34mfarewell cruel world ...\\033[0m\\n\"");
		b.append(".text");
		b.append("main:");

		stackOffset =  (spillCount+1-(arguments>4 ? arguments-4:0)) * 4;

		b.append("add $sp, $sp,",-stackOffset);
		b.append("sw $ra, 4($sp)");

		n.f10.accept(this);

		b.append("lw $ra, 4($sp)");
		b.append("add $sp, $sp, 4");
		b.append("add $v0, $0, 0");
		b.append("jr $ra");

		n.f12.accept(this);

		b.append(".alloc:");
		b.append("add $v0, $0, 9");
		b.append("syscall");
		b.append("jr $ra");
		b.append(".print:");
		b.append("add $v0, $0, 1");
		b.append("syscall");
		b.append("add $a0, $0, 10");
		b.append("add $v0, $0, 11");
		b.append("syscall");
		b.append("jr $ra");

		return b.toString();
	}

	/**
	 * Grammar production:
	 * f0 -> ( ( Label() )? Stmt() )*
	 */
	@Override
	public String visit(StmtList n) throws Exception {
		return n.f0.accept(this);
	}

	/**
	 * Grammar production:
	 * f0 -> Label()
	 * f1 -> "["
	 * f2 -> IntegerLiteral()
	 * f3 -> "]"
	 * f4 -> "["
	 * f5 -> IntegerLiteral()
	 * f6 -> "]"
	 * f7 -> "["
	 * f8 -> IntegerLiteral()
	 * f9 -> "]"
	 * f10 -> StmtList()
	 * f11 -> "END"
	 */
	@Override
	public String visit(Procedure n) throws Exception {
		String label = n.f0.f0.tokenImage;
		int stackOffset;
		arguments = Integer.parseInt(n.f2.f0.tokenImage);
		spillCount = Integer.parseInt(n.f5.f0.tokenImage);
		maxCount = Integer.parseInt(n.f8.f0.tokenImage);
		b.append(label+":");

		stackOffset =  (spillCount+1-(arguments>4 ? arguments-4:0)) * 4;

		b.append("add $sp, $sp,",-stackOffset);
		b.append("sw $ra, 4($sp)");

		n.f10.accept(this);

		b.append("lw $ra, 4($sp)");
		b.append("add $sp, $sp,",+stackOffset);
		b.append("jr $ra");

		return "Procedure";
	}

	/**
	 * Grammar production:
	 * f0 -> NoOpStmt()
	 *       | ErrorStmt()
	 *       | CJumpStmt()
	 *       | JumpStmt()
	 *       | HStoreStmt()
	 *       | HLoadStmt()
	 *       | MoveStmt()
	 *       | PrintStmt()
	 *       | ALoadStmt()
	 *       | AStoreStmt()
	 *       | PassArgStmt()
	 *       | CallStmt()
	 */
	@Override
	public String visit(Stmt n) throws Exception {
		return n.f0.accept(this);
	}

	/**
	 * Grammar production:
	 * f0 -> "NOOP"
	 */
	@Override
	public String visit(NoOpStmt n) throws Exception {
		/* Since $0 is hardwired to zero, this instruction has no effect and can be treated as a no operation. */
		b.append("sll $0, $0, 0");
		return "NoOpStmt";
	}

	/**
	 * Grammar production:
	 * f0 -> "ERROR"
	 */
	@Override
	public String visit(ErrorStmt n) throws Exception {
		b.append("li $v0, 4");
		b.append("la $a0, errormsg");
		b.append("syscall");
		b.append("li $v0, 10");
		b.append("syscall");
		return "ErrorStmt";
	}

	/**
	 * Grammar production:
	 * f0 -> "CJUMP"
	 * f1 -> Reg()
	 * f2 -> Label()
	 */
	@Override
	public String visit(CJumpStmt n) throws Exception {
		String target = n.f1.accept(this);
		String label = n.f2.accept(this);
		b.append(String.format("bne $%s, 1, %s",target,label));
		return "CJumpStmt";
	}

	/**
	 * Grammar production:
	 * f0 -> "JUMP"
	 * f1 -> Label()
	 */
	@Override
	public String visit(JumpStmt n) throws Exception {
		String label = n.f1.accept(this);
		b.append("j",label);
		return "JumpStmt";
	}

	/**
	 * Grammar production:
	 * f0 -> "HSTORE"
	 * f1 -> Reg()
	 * f2 -> IntegerLiteral()
	 * f3 -> Reg()
	 */
	@Override
	public String visit(HStoreStmt n) throws Exception {
		String target = n.f1.accept(this);
		String offset = n.f2.accept(this);
		String source = n.f3.accept(this);
		b.append(String.format("sw $%s, %s($%s)",source,offset,target));
		return "HStoreStmt";
	}

	/**
	 * Grammar production:
	 * f0 -> "HLOAD"
	 * f1 -> Reg()
	 * f2 -> Reg()
	 * f3 -> IntegerLiteral()
	 */

	@Override
	public String visit(HLoadStmt n) throws Exception {
		String target = n.f1.accept(this);
		String source = n.f2.accept(this);
		String offset = n.f3.accept(this);
		b.append(String.format("lw $%s, %s($%s)",target,offset,source));
		return "HLoadStmt";
	}

	/**
	 * Grammar production:
	 * f0 -> "MOVE"
	 * f1 -> Reg()
	 * f2 -> Exp()
	 */
	@Override
	public String visit(MoveStmt n) throws Exception {
		Node node = n.f2.f0.choice;
		String target = n.f1.accept(this);
		String pattern = n.f2.accept(this);
		if(node instanceof SimpleExp){
			node = ((SimpleExp) node).f0.choice;
			if(node instanceof Label)
				pattern = String.format("la $%%s, %s",pattern);
			if(node instanceof IntegerLiteral)
				pattern = String.format("add $%%s, $0, %s",pattern);
			if(node instanceof Reg)
				pattern = String.format("add $%%s, $%s, 0",pattern);
		}
		b.append(String.format(pattern,target));
		return "MoveStmt";
	}

	/**
	 * Grammar production:
	 * f0 -> "PRINT"
	 * f1 -> SimpleExp()
	 */
	@Override
	public String visit(PrintStmt n) throws Exception {
		String pattern = null;
		String exp = n.f1.accept(this);
		if(n.f1.f0.choice instanceof Reg)
			pattern = "add $a0, $0, $%s";
		if(n.f1.f0.choice instanceof IntegerLiteral)
			pattern = "add $a0, $0, %s";
		b.append(String.format(pattern,exp));
		b.append(String.format("jal .print"));
		return "PrintStmt";
	}

	/**
	 * Grammar production:
	 * f0 -> "ALOAD"
	 * f1 -> Reg()
	 * f2 -> SpilledArg()
	 */
	@Override
	public String visit(ALoadStmt n) throws Exception {
		String target = n.f1.accept(this);
		int offset = Integer.parseInt(n.f2.accept(this));
		b.append(String.format("lw $%s, %d($sp)",target,(spillCount-offset+1)*4));
		return "ALoadStmt";
	}

	/**
	 * Grammar production:
	 * f0 -> "ASTORE"
	 * f1 -> SpilledArg()
	 * f2 -> Reg()
	 */
	@Override
	public String visit(AStoreStmt n) throws Exception {
		int offset = Integer.parseInt(n.f1.accept(this));
		String source = n.f2.accept(this);
		b.append(String.format("sw $%s, %d($sp)",source,(spillCount-offset+1)*4));
		return "AStoreStmt";
	}

	/**
	 * Grammar production:
	 * f0 -> "PASSARG"
	 * f1 -> IntegerLiteral()
	 * f2 -> Reg()
	 */
	@Override
	public String visit(PassArgStmt n) throws Exception {
		int offset = Integer.parseInt(n.f1.f0.tokenImage);
		String register = n.f2.accept(this);
		++passargCount;
		b.append(String.format("sw $%s, %d($sp)",register,-(offset-1)*4));
		return "PassArgStmt";
	}

	/**
	 * Grammar production:
	 * f0 -> "CALL"
	 * f1 -> SimpleExp()
	 */
	@Override
	public String visit(CallStmt n) throws Exception {
		String register = n.f1.accept(this);
		if(passargCount > 0)
			b.append(String.format("add $sp, $sp, %d",-passargCount*4));
		b.append(String.format("jalr $%s",register));
		if(passargCount > 0)
			b.append(String.format("add $sp, $sp, %d",+passargCount*4));
		passargCount = 0;
		return "CallStmt";
	}

	/**
	 * Grammar production:
	 * f0 -> HAllocate()
	 *       | BinOp()
	 *       | SimpleExp()
	 */
	@Override
	public String visit(Exp n) throws Exception {
		return n.f0.accept(this);
	}

	/**
	 * Grammar production:
	 * f0 -> "HALLOCATE"
	 * f1 -> SimpleExp()
	 */
	@Override
	public String visit(HAllocate n) throws Exception {
		String pattern = null;
		String exp = n.f1.accept(this);
		if(n.f1.f0.choice instanceof Reg)
			pattern = "add $a0, $0, $%s";
		if(n.f1.f0.choice instanceof IntegerLiteral)
			pattern = "add $a0, $0, %s";
		b.append(String.format(pattern,exp));
		b.append("jal .alloc");
		return "add $%s, $v0, 0";
	}

	/**
	 * Grammar production:
	 * f0 -> Operator()
	 * f1 -> Reg()
	 * f2 -> SimpleExp()
	 */
	@Override
	public String visit(BinOp n) throws Exception {
		String pattern = null;
		String operator = n.f0.accept(this);
		String loperand = n.f1.accept(this);
		String roperand = n.f2.accept(this);
		if(n.f2.f0.choice instanceof Reg)
			pattern = "%s $%%s, $%s, $%s";
		if(n.f2.f0.choice instanceof IntegerLiteral)
			pattern = "%s $%%s, $%s, %s";
		return String.format(pattern,op2asm.get(operator),loperand,roperand);	//hacky stuff :P
	}

	/**
	 * Grammar production:
	 * f0 -> "LT"
	 *       | "PLUS"
	 *       | "MINUS"
	 *       | "TIMES"
	 */
	@Override
	public String visit(Operator n) throws Exception {
		return n.f0.accept(this);
	}

	/**
	 * Grammar production:
	 * f0 -> "SPILLEDARG"
	 * f1 -> IntegerLiteral()
	 */
	@Override
	public String visit(SpilledArg n) throws Exception {
		return n.f1.accept(this);
	}

	/**
	 * Grammar production:
	 * f0 -> Reg()
	 *       | IntegerLiteral()
	 *       | Label()
	 */
	@Override
	public String visit(SimpleExp n) throws Exception {
		return n.f0.accept(this);
	}

	/**
	 * Grammar production:
	 * f0 -> "a0"
	 *       | "a1"
	 *       | "a2"
	 *       | "a3"
	 *       | "t0"
	 *       | "t1"
	 *       | "t2"
	 *       | "t3"
	 *       | "t4"
	 *       | "t5"
	 *       | "t6"
	 *       | "t7"
	 *       | "s0"
	 *       | "s1"
	 *       | "s2"
	 *       | "s3"
	 *       | "s4"
	 *       | "s5"
	 *       | "s6"
	 *       | "s7"
	 *       | "t8"
	 *       | "t9"
	 *       | "v0"
	 *       | "v1"
	 */
	@Override
	public String visit(Reg n) throws Exception {
		return n.f0.accept(this);
	}

	/**
	 * Grammar production:
	 * f0 -> <INTEGER_LITERAL>
	 */
	@Override
	public String visit(IntegerLiteral n) throws Exception {
		return n.f0.accept(this);
	}

	/**
	 * Grammar production:
	 * f0 -> <IDENTIFIER>
	 */
	@Override
	public String visit(Label n) throws Exception {
		return n.f0.accept(this);
	}
}
