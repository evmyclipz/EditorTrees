package editortrees;

public class BContainer { 	// boolean Container
	boolean isBalanced;
	int rotCount;
	public char data;
	boolean proceedBCchanges; // marks if balance code (going towards root) should be changing
	
	public BContainer(boolean isBalanced, int rotCount) {
		this.isBalanced = isBalanced;
		this.rotCount = rotCount;
		this.proceedBCchanges = true;
	}
}
