/*
Name: Clinton J. Schultz
Professor: Dr. Jeff Ward
Assignment: HW5 - AVL Trees
Date: 11/3/2020

This program is designed to improve runtime performance of inorder iterators.
This java file ('AVLTreeWithFastIterator.java') is tested against 'AVLTree.java'
by 'TestAVLTreeIterator.java', and 'AVLTreeWithFastIterator.java' should provide
improvement upon the results of running the same data through 'AVLTree.java'.
 */


import java.util.Iterator;

public class AVLTreeWithFastIterator<E extends Comparable<E>> extends BST<E> {
    /** Create an empty AVL tree */
    public AVLTreeWithFastIterator() { }

    @Override /** Override createNewNode to create an AVLTreeNode */
    protected AVLTreeNode<E> createNewNode(E e) { return new AVLTreeNode<E>(e); }

    /** Create an AVL tree from an array of objects */
    public AVLTreeWithFastIterator(E[] objects) { super(objects); }

    @Override
    public Iterator<E> iterator() { return new AVLTreeWithFastIterator.FastIterator(); }

    @Override
    public Iterator<E> iterator(int index) { return new AVLTreeWithFastIterator.FastIterator(index); }

    private class FastIterator implements Iterator<E> {
        private TreeNode<E> current = root;
        private java.util.Stack<TreeNode<E>> stack = new java.util.Stack<>();
        private E lastReturned = null;

        // default constructer
        public FastIterator() {}

        // constructor from pdf while loop
        public FastIterator(int numToSkip) {

            if (numToSkip < 0 || numToSkip > size()) {
                throw new IndexOutOfBoundsException();
            }
            while (numToSkip > 0) {
                if (current.left == null) {
                    if (numToSkip == 1) {
                        current = current.right;
                        numToSkip = 0;
                    } else {
                        current = null;
                        numToSkip = 0;
                    }
                } else if (numToSkip == ((AVLTreeNode<E>) (current.left)).size) {
                    stack.push(current);
                    current = null;
                    numToSkip = 0;
                } else if (numToSkip < ((AVLTreeNode<E>) (current.left)).size) {
                    stack.push(current);
                    current = current.left;
                } else {
                    numToSkip -= (((AVLTreeNode<E>)(current.left)).size) + 1;
                    current = current.right;
                }
            }
        }


        public boolean hasNext() { return (current != null || !stack.empty()); }

        public E next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException();
            }
            while (current != null) {
                stack.push(current);
                current = current.left;
            }
            TreeNode<E> poppedNode = stack.pop();
            lastReturned = poppedNode.element;
            current = poppedNode.right;
            return lastReturned;
        }
        public void remove() {
            throw new UnsupportedOperationException("Invalid operation for sorted list");
        }
    }

    @Override /** Insert an element and rebalance if necessary */
    public boolean insert(E e) {
        boolean successful = super.insert(e);
        if (!successful) {
            return false; // if e is in tree already
        }
        else {
            balancePath(e); // from e to root if necessary
        }
        return true; // if e is inserted
    }

    /** Update the height of a specified node */
    private void updateHeightandSize(AVLTreeNode<E> node) {
        if (node.left == null && node.right == null) { //the node is a leaf
            node.height = 0;
            node.size = 1;
        }
        else if (node.left == null) { // no left subtree
            node.height = 1 + ((AVLTreeNode<E>) (node.right)).height;
            node.size = 1 + ((AVLTreeNode<E>) (node.right)).size;
        }
        else if (node.right == null) { // no right subtree
            node.height = 1 + ((AVLTreeNode<E>) (node.left)).height;
            node.size = 1 + ((AVLTreeNode<E>) (node.left)).size;
        }
        else {
            node.height = 1 + Math.max(((AVLTreeNode<E>) (node.right)).height,
                    ((AVLTreeNode<E>) (node.left)).height);
            node.size = 1 + ((AVLTreeNode<E>) (node.right)).size + ((AVLTreeNode<E>) (node.left)).size;
        }
    }
    /** Balance the nodes in the path from the specified
     * node to the root if necessary
     */
    private void balancePath(E e) {
        java.util.ArrayList<TreeNode<E>> path = path(e);
        for (int i = path.size() - 1; i >= 0; i--) {
            AVLTreeNode<E> A = (AVLTreeNode<E>)(path.get(i));
            updateHeightandSize(A);
            AVLTreeNode<E> parentOfA = (A == root) ? null :
                    (AVLTreeNode<E>)(path.get(i - 1));

            switch (balanceFactor(A)) {
                case -2:
                    if (balanceFactor((AVLTreeNode<E>)A.left) <= 0) {
                        balanceLL(A, parentOfA); // Perform LL rotation
                    }
                    else {
                        balanceLR(A, parentOfA); // Perform LR rotation
                    }
                    break;
                case +2:
                    if (balanceFactor((AVLTreeNode<E>)A.right) >= 0) {
                        balanceRR(A, parentOfA); // Perform RR rotation
                    }
                    else {
                        balanceRL(A, parentOfA); // Perform RL rotation
                    }
            }
        }
    }
    /** Return the balance factor of the node */
    private int balanceFactor(AVLTreeNode<E> node) {
        if (node.right == null) { // node has no right subtree
            return -node.height;
        }
        else if (node.left == null) { // node has no left subtree
            return +node.height;
        }
        else {
            return ((AVLTreeNode<E>)node.right).height - ((AVLTreeNode<E>)node.left).height;
        }
    }

    /** Balance LL (see Figure 27.1) */
    private void balanceLL(TreeNode<E> A, TreeNode<E> parentOfA) {
        TreeNode<E> B = A.left; // A is left-heavy and B is left-heavy

        if (A == root) {
            root = B;
        }
        else {
            if (parentOfA.left == A) {
                parentOfA.left = B;
            }
            else {
                parentOfA.right = B;
            }
        }
        A.left = B.right; // Make T2 left subtree of A
        B.right = A; // Make A left child of B
        updateHeightandSize((AVLTreeNode<E>)A);
        updateHeightandSize((AVLTreeNode<E>)B);
    }
    /** Balance LR (see Figure 27.1c) */
    private void balanceLR(TreeNode<E> A, TreeNode<E> parentOfA) {
        TreeNode<E> B = A.left; // A is left-heavy
        TreeNode<E> C = B.right; // B is right-heavy

        if (A == root) {
            root = C;
        }
        else {
            if (parentOfA.left == A) {
                parentOfA.left = C;
            }
            else {
                parentOfA.right = C;
            }
        }
        A.left = C.right; // Make T3 the left subtree of A
        B.right = C.left; // Make T2 the right subtree of B
        C.left = B;
        C.right = A;
        updateHeightandSize((AVLTreeNode<E>)A);
        updateHeightandSize((AVLTreeNode<E>)B);
        updateHeightandSize((AVLTreeNode<E>)C);
    }
    /** Balance RR (see Figure 27.1b) */
    private void balanceRR(TreeNode<E> A, TreeNode<E> parentOfA) {
        TreeNode<E> B = A.right; // A and B are right-heavy

        if (A == root) {
            root = B;
        }
        else {
            if (parentOfA.left == A) {
                parentOfA.left = B;
            }
            else {
                parentOfA.right = B;
            }
        }
        A.right = B.left; // Make T2 the right subtree of A
        B.left = A;
        updateHeightandSize((AVLTreeNode<E>)A);
        updateHeightandSize((AVLTreeNode<E>)B);
    }
    /** Balance RL (see Figure 27.1d) */
    private void balanceRL(TreeNode<E> A, TreeNode<E> parentOfA) {
        TreeNode<E> B = A.right; // A is right-heavy
        TreeNode<E> C = B.left; // B is left-heavy

        if (A == root) {
            root = C;
        }
        else {
            if (parentOfA.left == A) {
                parentOfA.left = C;
            }
            else {
                parentOfA.right = C;
            }
        }
        A.right = C.left; // Make T2 the right subtree of A
        B.left = C.right; // Make T3 the left subtree of B
        C.left = A;
        C.right = B;
        updateHeightandSize((AVLTreeNode<E>)A);
        updateHeightandSize((AVLTreeNode<E>)B);
        updateHeightandSize((AVLTreeNode<E>)C);
    }
    @Override /** Delete an element from the binary tree.
     * Return true if the element is deleted successfully
     * Return false if the element is not in the tree */
    public boolean delete(E element) {
        if (root == null) {
            return false; // Element is not in the tree
        }
        // Locate the node to be deleted and also locate its parent node
        TreeNode<E> parent = null;
        TreeNode<E> current = root;
        while (current != null) {
            if (element.compareTo(current.element) < 0) {
                parent = current;
                current = current.left;
            }
            else if (element.compareTo(current.element) > 0) {
                parent = current;
                current = current.right;
            }
            else {
                break; // Element is in the tree pointed by current
            }
        }
        if (current == null) {
            return false; // Element is not in the tree
        }
        // Case 1: current has no left children (See Figure 23.6)
        if (current.left == null) {
            // Connect the parent with the right child of the current node
            if (parent == null) {
                root = current.right;
            }
            else {
                if (element.compareTo(parent.element) < 0) {
                    parent.left = current.right;
                }
                else {
                    parent.right = current.right;
                }
                // Balance the tree if necessary
                balancePath(parent.element);
            }
        }
        else {
            // Case 2: The current node has a left child
            // Locate the rightMost node in the left subtree of
            // the current node and also its parent
            TreeNode<E> parentOfRightMost = current;
            TreeNode<E> rightMost = current.left;

            while (rightMost.right != null) {
                parentOfRightMost = rightMost;
                rightMost = rightMost.right; // Keep going to the right
            }
            // Replace the element in current by the element in rightMost
            current.element = rightMost.element;

            // Eliminate rightmost node
            if (parentOfRightMost.right == rightMost) {
                parentOfRightMost.right = rightMost.left;
            }
            else {
                // Special case: parentOfRightMost is current
                parentOfRightMost.left = rightMost.left;
            }
            // Balance the tree if necessary
            balancePath(parentOfRightMost.element);
        }
        size--;
        return true; // Element inserted
    }
    /** AVLTreeNode is TreeNode plus height */
    protected static class AVLTreeNode<E> extends TreeNode<E> {

        protected int height = 0; // New data field
        private int size = 0;
        public void setSize(int size) {
            this.size = size;
        }
        public int getSize() {
            return size;
        }
        public AVLTreeNode(E o) {
            super(o);
        }
    }
}
