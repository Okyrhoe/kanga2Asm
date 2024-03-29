//
// Generated by JTB 1.3.2 DIT@UoA patched
//

package syntaxtree;

/**
 * Grammar production:
 * f0 -> "HLOAD"
 * f1 -> Reg()
 * f2 -> Reg()
 * f3 -> IntegerLiteral()
 */
public class HLoadStmt implements Node {
   public NodeToken f0;
   public Reg f1;
   public Reg f2;
   public IntegerLiteral f3;

   public HLoadStmt(NodeToken n0, Reg n1, Reg n2, IntegerLiteral n3) {
      f0 = n0;
      f1 = n1;
      f2 = n2;
      f3 = n3;
   }

   public HLoadStmt(Reg n0, Reg n1, IntegerLiteral n2) {
      f0 = new NodeToken("HLOAD");
      f1 = n0;
      f2 = n1;
      f3 = n2;
   }

   public void accept(visitor.Visitor v) throws Exception {
      v.visit(this);
   }
   public <R,A> R accept(visitor.GJVisitor<R,A> v, A argu) throws Exception {
      return v.visit(this,argu);
   }
   public <R> R accept(visitor.GJNoArguVisitor<R> v) throws Exception {
      return v.visit(this);
   }
   public <A> void accept(visitor.GJVoidVisitor<A> v, A argu) throws Exception {
      v.visit(this,argu);
   }
}

