package editortrees;

import java.util.ArrayList;


/**
 * A node in a height-balanced binary tree with rank. Except for the NULL_NODE,
 * one node cannot belong to two different trees.
 * 
 * @author <<Rohan Malipeddi and Jimin Park>> 
 */
public class Node   {

	enum Code {
		SAME, LEFT, RIGHT;

		// Used in the displayer and debug string
		public String toString() {
			switch (this) {
			case LEFT:
				return "/";
			case SAME:
				return "=";
			case RIGHT:
				return "\\";
			default:
				throw new IllegalStateException();
			}
		}
	}

	// The fields would normally be private, but for the purposes of this class,
	// we want to be able to test the results of the algorithms in addition to the
	// "publicly visible" effects

	char data;
	Node left, right; // subtrees
	int rank; // inorder position of this node within its own subtree.
	Code balance;
	public DisplayableNodeWrapper displayableNodeWrapper;

	// Feel free to add other fields that you find useful.
	// You probably want a NULL_NODE, but you can comment it out if you decide
	// otherwise.
	// The NULL_NODE uses the "null character", \0, as it's data and null children,
	// but they could be anything since you shouldn't ever actually refer to them in
	// your code.
	static final Node NULL_NODE = new Node('\0', null, null);
	// Node parent; You may want parent, but think twice: keeping it up-to-date
	// takes effort too, maybe more than it's worth.

	public Node(char data, Node left, Node right) {
		this.data = data;
		this.left = left;
		this.right = right;
		rank = 0;	// rank is 0 bc of new node
		balance = Code.SAME;
		displayableNodeWrapper = new DisplayableNodeWrapper(this);
	}

	public Node(char data) {
		// Make a leaf
		this(data, NULL_NODE, NULL_NODE);
	}
	
	public Node(char data, Node left, Node right, int rank, Code balance) {
		this.data = data;
		this.left = left;
		this.right = right;
		this.rank = rank;	// rank is 0 bc of new node
		this.balance = balance;
		displayableNodeWrapper = new DisplayableNodeWrapper(this);
	}

	// Provided to you to enable testing, please don't change.
	int slowHeight() {
		if (this == NULL_NODE) {
			return -1;
		}
		return Math.max(left.slowHeight(), right.slowHeight()) + 1;
	}

	// Provided to you to enable testing, please don't change.
	public int slowSize() {
		if (this == NULL_NODE) {
			return 0;
		}
		return left.slowSize() + right.slowSize() + 1;
	}

	public void InOrderToString(StringBuilder result) {
		if(this == NULL_NODE) return;
		left.InOrderToString(result);
		result.append(this.data);
		right.InOrderToString(result);
	}
	
	/**
	 * Single left rotation helper method
	 * @param curr
	 * @return new root node
	 */
	public Node singleLeft(Node curr) {
		Node newRoot = curr.right;
		Node leftOfRoot = newRoot.left;
		newRoot.left = curr;
		
		curr.right = leftOfRoot;
		curr.balance = Code.SAME;
		
		newRoot.balance = Code.SAME; // it is always the the same no matter what
		
		newRoot.rank = newRoot.rank + curr.rank + 1;
		
		return newRoot;
	}
	
	/**
	 * Single right rotation helper method
	 * @param curr
	 * @return new root node
	 */
	public Node singleRight(Node curr) {
		Node newRoot = curr.left;
		Node rightOfRoot = newRoot.right; // save the right value
		newRoot.right = curr;
		
		curr.left = rightOfRoot; // put orphaned child below curr (which was recently moved)
		curr.balance = Code.SAME;
		
		newRoot.balance = Code.SAME;
		
		curr.rank = curr.rank - newRoot.rank - 1;
		
		return newRoot;
	}
	
	
	public Node add(char ch, int pos, BContainer b) {
		if(this == NULL_NODE) {
			return new Node(ch);
		}
		if(pos <= rank) { 		// add to left
			rank++;				// update rank
			left = left.add(ch, pos, b);
			
			return getLeftBalanced(b);
		}
		else {	// add to right
			pos = pos - (rank + 1);	// update position 
			right = right.add(ch, pos, b);
			
			return getRightBalanced(b);
		}
	}
	
	private Node getRightBalanced(BContainer b) {
		if(b.isBalanced) { // if already balanced, why do it again, don't need it
			return this;
		}
		if(this.balance.equals(Code.RIGHT) && this.right.balance.equals(Code.LEFT)) {
			// do double left rotation
			b.isBalanced = true;
			return doubleLeftRotation(b);
		}
		else if(balance.equals(Code.SAME)) {
			balance = Code.RIGHT;
		}
		else if(balance.equals(Code.LEFT)){
			balance = Code.SAME;
			b.isBalanced = true;
		}
		else if(balance.equals(Code.RIGHT)){ // balance code was right
			b.rotCount++;
			b.isBalanced = true;
			return singleLeft(this);
		}
		
		return this;
	}
	
	private Node doubleLeftRotation(BContainer b) {
		b.rotCount++;
		// have to remember which subtree is attached the node to
		Code tempBalanceOfC = this.right.left.balance;
		this.right = singleRight(this.right);
		b.rotCount++;
		Node temp = singleLeft(this);
		if(tempBalanceOfC.equals(Code.LEFT)) { 	// based on the activity done in class
			temp.right.balance = Code.RIGHT;
		}
		else if(tempBalanceOfC.equals(Code.RIGHT)) {
			temp.left.balance = Code.LEFT;
		}
		return temp;
	}
	
	
	private Node getLeftBalanced(BContainer b) {
		if(b.isBalanced) {
			return this;
		}
		// Double Right Rotation
		if(this.balance.equals(Code.LEFT) && this.left.balance.equals(Code.RIGHT)) {
			b.isBalanced = true;
			return doubleRightRotation(b);
		}
		if(this.balance.equals(Code.SAME)) {
			this.balance = Code.LEFT;
		}
		else if(this.balance.equals(Code.RIGHT)) {
			this.balance = Code.SAME;
			b.isBalanced = true;
		}
		else if(this.balance.equals(Code.LEFT)){
			b.rotCount++;
			b.isBalanced = true;	// making isBalanced before we do rotation
			return singleRight(this);
		}
		
		return this;
	}
	
	private Node doubleRightRotation(BContainer b) {
		b.rotCount++;
		// have to remember which subtree is attached the node to
		Code tempBalanceOfC = this.left.right.balance; 
		this.left = singleLeft(this.left);
		b.rotCount++;
		Node temp = singleRight(this);
		if(tempBalanceOfC.equals(Code.LEFT)) { // based on activty done in class
			temp.right.balance = Code.RIGHT;
		}
		else if(tempBalanceOfC.equals(Code.RIGHT)) {
			temp.left.balance = Code.LEFT;
		}
		return temp;
	}

	public void PreOrderToRankString(StringBuilder result) {
		if(this == NULL_NODE) {
			return;
		}
		result.append(this.data); // these 3 statements are done after another because adding character and rank gives us funky values
		result.append(rank);
		result.append(", ");
		left.PreOrderToRankString(result);
		right.PreOrderToRankString(result);
	}

	public char get(int pos) {		// same as add at pos
		if(pos < rank) {
			return left.get(pos);
		}
		else if(pos > rank) {
			pos = pos - (rank+1);
			return right.get(pos);
		}
		else {
			return this.data;
		}
	}

	public LeftTreeSize rankMatchesLeftSize() {		// uses a container class and propagates the size and rankChecker
		if(this == NULL_NODE) {
			return new LeftTreeSize(0, true); 	// to prevent stack overflow
		}
		LeftTreeSize leftRank = left.rankMatchesLeftSize();
		LeftTreeSize rightRank = right.rankMatchesLeftSize();
		int size = leftRank.size+rightRank.size+1; // propagates the size
		boolean isEqual = (leftRank.rankEqualSize == false || rightRank.rankEqualSize == false || rank != leftRank.size) ? false : true;
		return new LeftTreeSize(size, isEqual);
	}

	public void PreOrderToDebugString(StringBuilder result) {
		if(this == NULL_NODE) {
			return;
		}
		result.append(this.data); // these 3 statements are done after another because adding character and rank gives us funky values
		result.append(rank);
		result.append(this.balance);
		result.append(", ");
		left.PreOrderToDebugString(result);
		right.PreOrderToDebugString(result);
	}

	// following 4 methods are for displaying the tree
	public Node getParent() {
		return this;
	}

	public boolean hasLeft() {
		return this.left != NULL_NODE;
	}
	
	public boolean hasRight() {
		return this.right != NULL_NODE;
	}

	public boolean hasParent() {
		// TODO Auto-generated method stub
		return this != NULL_NODE ? true : false;
	}
	
	public BalanceCodeChecker checkBalanceCodes() {
		if(this == NULL_NODE) {
			return new BalanceCodeChecker(true, -1); // true bc null node is always balanced
		}
		BalanceCodeChecker leftTree = left.checkBalanceCodes();
		BalanceCodeChecker rightTree = right.checkBalanceCodes();
		boolean isCorrect = false;
		int height = Math.max(leftTree.height, rightTree.height) + 1;
		//if either of the children are wrong, then the current is also wrong
		if(!(leftTree.isCorrect && rightTree.isCorrect)) {
			return new BalanceCodeChecker(false, height);
		}
		// to determine the boolean value
		if(leftTree.height == rightTree.height && this.balance == Code.SAME) {
			isCorrect = true;
		}
		else if(leftTree.height > rightTree.height && this.balance == Code.LEFT) {
			isCorrect = true;
		}
		else if(leftTree.height < rightTree.height && this.balance == Code.RIGHT) {
			isCorrect = true;
		}
		return new BalanceCodeChecker(isCorrect, height); // propogate the boolean value upwards
	}

	public int getFastHeight() {
		if(this == NULL_NODE) {
			return -1;
		}
		int leftheight=0;
		int rightheight=0;
		if(this.balance.equals(Code.SAME)) { //we check either tree 	
			//leftheight = left.getFastHeight();
			rightheight = right.getFastHeight();
		}
		else if(this.balance.equals(Code.LEFT)) {
			leftheight = left.getFastHeight();
		}
		else {
			rightheight = right.getFastHeight();
		}
		return Math.max(leftheight, rightheight)+1;
	}

	public Node delete(int pos, BContainer container) {
		if(this == NULL_NODE) {
			return this;
		}
		if(pos < rank) {
			left = left.delete(pos, container);
			return getDeleteLeftBalance(container);
		}
		else if(pos > rank) {
			pos = pos - (rank+1);
			right = right.delete(pos, container);
			return getDeleteRightBalance(container);
		}
		else { //initiate deletion
			container.data = this.data;
			int numChildren = getChildren();
			if(numChildren == 0) { //deleting a leaf node
				return NULL_NODE;
			}
			else if(numChildren == 1) { // deleting node with 1 child
				if(this.left != NULL_NODE) {
					return this.left;
				}
				else
					return this.right;
			}
			else if(numChildren == 2){ // deleting node with 2 children
				// hibbard deletion with successor
				right = right.delete(0, container);
				
				// swap right most data with current data
				char temp = container.data;
				container.data = this.data;
				this.data = temp;
				
				return getDeleteRightBalance(container);
			}
			return this;
		}
		
	}
	
	/**
	 * Returns the left subtree that has been balanced.
	 * This is to be done after deleting the desired element.
	 * 
	 * @param container
	 * @return
	 */
	private Node getDeleteLeftBalance(BContainer container) {
//		if(container.isBalanced)
//			return this;
		this.rank--;
		if(this.balance == Code.RIGHT && !container.isBalanced) {
			if(this.right.balance == Code.LEFT) { // do double left rotation
				return doubleLeftRotation(container);
			}
			else if(this.right.balance == Code.RIGHT) { // do sl rotation
//				container.isBalanced = true;
				container.rotCount++;
				return singleLeft(this);
			}
			else if(this.right.balance == Code.SAME)  {  // edge case
				container.rotCount++;
				Node temp = singleLeft(this);
				temp.balance = Code.LEFT;
				temp.left.balance = Code.RIGHT;
				container.proceedBCchanges = false;
				return temp;
			}
		}
		else if(this.balance == Code.SAME && container.proceedBCchanges) {
			this.balance = Code.RIGHT;
			container.isBalanced = true;
			container.proceedBCchanges = false; // height of tree did not change
		}
		else if(container.proceedBCchanges){ // balance code is to the left
			this.balance = Code.SAME;
		}
		// else if is balanced
		return this;
	}
	
	private Node getDeleteRightBalance(BContainer container) {
//		if(container.isBalanced)
//			return this;
		
		if(this.balance == Code.LEFT && !container.isBalanced) {
			if(this.left.balance == Code.RIGHT) {
				return doubleRightRotation(container);
			}
			else if(this.left.balance == Code.LEFT) { 	// do sr rotation
//				container.isBalanced = true;
				container.rotCount++;
				return singleRight(this);
			}
			else if(this.left.balance == Code.SAME) {  // edge case 
				container.rotCount++;
				Node temp = singleRight(this);
				temp.balance = Code.RIGHT;
				temp.right.balance = Code.LEFT;
				container.proceedBCchanges = false;
				return temp;
			}
		}
		else if(this.balance == Code.SAME && container.proceedBCchanges) {
			this.balance = Code.LEFT;
			container.isBalanced = true;
			container.proceedBCchanges = false;
		}
		else if(container.proceedBCchanges){ // balance code is to the right
			this.balance = Code.SAME;
		}
		return this;
	}
	
	// helper method for delete
	private int getChildren() {
		int result = 0;
		if(this.hasLeft()) {
			result++;
		}
		if(this.hasRight()) {
			result++;
		}
		return result;
	}

	/*
	 * the nodes being added/traversed are in the range [pos, pos+length-1]
	 * -> each node has access to pos: starting pos of desired substrin
	 * 							  length: length of the substring
	 * 							  currPos: pos of this node in an in-order traversal
	 */
	public void InOrderGetToString(StringBuilder result, int pos, int length, int currentPos) {
        // If the current node is null, just return
        if (this == NULL_NODE) {
            return;
        }
        int leftSize;
        // Calculate where in the subtree this node's content starts
        if(left != NULL_NODE)
        	leftSize = this.rank;
        else
        	leftSize = 0;
        
        // Traverse the left subtree if it overlaps the target range 
        // add currPos because we have to know the pos in reference to the start of range
        // if less, go left
        if (pos < currentPos + leftSize) { 
            left.InOrderGetToString(result, pos, length, currentPos);
        }
        
        // Add this node’s data if within the range
        // if the node is in range [pos, pos+length-1]
        int thisPos = currentPos + leftSize;
        if (thisPos >= pos && thisPos < pos + length) { 
            result.append(this.data);
        }
        
        // Traverse the right subtree if it overlaps the target range
        // new range starting from root to end of range
        if (thisPos < pos + length - 1) {
            right.InOrderGetToString(result, pos, length, thisPos + 1);
        }
    }
	
	// failures for get
	
//	if(lb == 0 && rb == 0) {
//		result.append(this.data);
//		return;
//	}
//	// going left
//	if(lb < rank) {
//		if(rb < rank) 
//			rb = rb;
//		else 
//			rb = rb-1;
//		goLeft(result, lb, rb);
//	}
//	// going to the right
//	if(rb > rank) {
//		result.append(this.data);	// add this since we are going to the right
//		int temp = rank + 1;
//		rb = rb - temp;
//		lb = rank + 1 - temp;
//		right.InOrderGetToString(result, lb, rb);
//	}
//	return;
	
//	private void goLeft(StringBuilder result, int lb, int rb) {
//		// TODO Auto-generated method stub
//		if(lb == 0 && rb == 0) {
//			result.append(this.data);
//		}
//		else if(lb == rank) {
//			result.append(this.data);
//		}
//		else if(LB < rank) {
//			int temp = this.rank + 1;
//			lb = 0;
//			rb = rb - temp;
//			goRight(result, lb, rb);
//		}
//		return;
//	}
//	
//	private void goRight(StringBuilder result, int lb, int rb) {
//		// TODO Auto-generated method stub
//		if(lb == 0 && rb == 0) {
//			result.append(this.data);
//		}
//		else if(rb == rank) {
//			rb = rb - 1;
//			goLeft(result, lb, rb);
//			result.append(this.data);
//		}
//		return;
//	}
	

//	if(this == NULL_NODE) {
//		return;
//	}
//	int leftSize;
//	if(left != NULL_NODE) 
//		leftSize = this.rank;
//	else 
//		leftSize = 0;
//	if(pos < currpos + leftSize) {	// check if the pos is less than the rank + the node its currently on
//		left.InOrderGetToString(result, pos, length, currpos);  // 
//	}
	
}
	
	
	// You will probably want to add more constructors and many other
	// recursive methods here. I added 47 of them - most were tiny helper methods
	// to make the rest of the code easy to understand. My longest method was
	// delete(): 20 lines of code other than } lines. Other than delete() and one of
	// its helpers, the others were less than 10 lines long. Well-named helper
	// methods are more effective than comments in writing clean code.
	
	// TODO: By the end of milestone 1, consider if you want to use the graphical debugger. See
	// the unit test throwing an error and the README.txt file.
