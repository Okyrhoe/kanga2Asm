//
// Generated by JTB 1.3.2 DIT@UoA patched
//

package syntaxtree;

/**
 * Grammar production:
 * f0 -> "MOVE"
 * f1 -> Reg()
 * f2 -> Exp()
 */
public class MoveStmt implements Node {
   public NodeToken f0;
   public Reg f1;
   public Exp f2;

   public MoveStmt(NodeToken n0, Reg n1, Exp n2) {
      f0 = n0;
      f1 = n1;
      f2 = n2;
   }

   public MoveStmt(Reg n0, Exp n1) {
      f0 = new NodeToken("MOVE");
      f1 = n0;
      f2 = n1;
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

