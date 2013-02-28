package net.sf.saxon.tree.tiny;

import net.sf.saxon.om.CodedName;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.AxisIteratorImpl;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.Value;

/**
* AttributeEnumeration is an iterator over all the attribute nodes of an Element.
*/

final class AttributeEnumeration extends AxisIteratorImpl {

    private TinyTree tree;
    private int element;
    private NodeTest nodeTest;
    private int index;
    private int currentNodeNr;

    /**
    * Constructor. Note: this constructor will only be called if the relevant node
    * is an element and if it has one or more attributes. Otherwise an EmptyEnumeration
    * will be constructed instead.
    * @param tree: the containing TinyTree
    * @param element: the node number of the element whose attributes are required
    * @param nodeTest: condition to be applied to the names of the attributes selected
    */

    AttributeEnumeration(/*@NotNull*/ TinyTree tree, int element, NodeTest nodeTest) {

        this.nodeTest = nodeTest;
        this.tree = tree;
        this.element = element;
        index = tree.alpha[element];
        currentNodeNr = -1;
    }

    /**
    * Move to the next node in the iteration.
    */

    public boolean moveNext() {
        while (true) {
            if (index >= tree.numberOfAttributes || tree.attParent[index] != element) {
                index = Integer.MAX_VALUE;
                current = null;
                position = -1;
                currentNodeNr = -1;
                return false;
            }
            int typeCode = tree.getAttributeAnnotation(index);
            if (nodeTest.matches(Type.ATTRIBUTE, new CodedName(tree.attCode[index], tree.getNamePool()), typeCode)) {
                position++;
                currentNodeNr = index++;
                if (nodeTest instanceof NameTest) {
                    // there can only be one match, so abandon the search after this node
                    index = Integer.MAX_VALUE;
                }
                current = null;
                return true;
            }
            index++;
        }
    }

    /**
     * Get the next item in the sequence. <BR>
     *
     * @return the next Item. If there are no more nodes, return null.
     */

    /*@Nullable*/ public NodeInfo next() {
        if (moveNext()) {
            current = tree.getAttributeNode(currentNodeNr);
        } else {
            current = null;
        }
        return current;
    }

    /**
     * Get the current node in the sequence.
     *
     * @return the node returned by the most recent call on next(), or the node on which we positioned using
     * moveNext()
     */

    /*@Nullable*/ public NodeInfo current() {
        if (current == null) {
            if (currentNodeNr == -1) {
                return null;
            } else {
                current = tree.getAttributeNode(currentNodeNr);
            }
        }
        return current;
    }

    /**
     * Return the atomized value of the current node.
     *
     * @return the atomized value.
     * @throws NullPointerException if there is no current node
     */

    public Value atomize() throws XPathException {
        if (currentNodeNr == -1) {
            throw new NullPointerException();
        }
        return tree.getTypedValueOfAttribute(null, currentNodeNr);
    }

    /**
     * Return the string value of the current node.
     *
     * @return the string value, as an instance of CharSequence.
     * @throws NullPointerException if there is no current node
     */

    public CharSequence getStringValue() {
        if (currentNodeNr == -1) {
            throw new NullPointerException();
        }
        return tree.attValue[currentNodeNr];
    }

    /**
    * Get another iteration over the same nodes
    */

    /*@NotNull*/ public AxisIterator getAnother() {
        return new AttributeEnumeration(tree, element, nodeTest);
    }

}

//
// The contents of this file are subject to the Mozilla Public License Version 1.0 (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the
// License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations under the License.
//
// The Original Code is: all this file
//
// The Initial Developer of the Original Code is Saxonica Limited.
// Portions created by ___ are Copyright (C) ___. All rights reserved.
//
// Contributor(s):
//