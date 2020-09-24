public class AST {
    private Node_AST root;

    public AST(Node_AST root){
        this.root = root;
    }

    public AST(Token token){
        this.root = new Node_AST(token, null);
    }

    public Node_AST getRoot() {
        return root;
    }
}