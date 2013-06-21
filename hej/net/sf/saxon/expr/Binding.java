package net.sf.saxon.expr;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;

/**
* Binding is a interface used to represent the run-time properties and methods
* associated with a variable: specifically, a method to get the value
* of the variable.
*/

public interface Binding  {

    /**
     * Get the declared type of the variable
     * @return the declared type
     */

    public SequenceType getRequiredType();

    /**
     * If the variable is bound to an integer, get the minimum and maximum possible values.
     * Return null if unknown or not applicable
     * @return a pair of integers containing the minimum and maximum values for the integer value;
     * or null if the value is not an integer or the range is unknown
     */

    /*@Nullable*/ public IntegerValue[] getIntegerBoundsForVariable();

    /**
     * Evaluate the variable
     * @param context the XPath dynamic evaluation context
     * @return the result of evaluating the variable
     * @throws net.sf.saxon.trans.XPathException if an error occurs while evaluating
     * the variable
    */

    public ValueRepresentation<? extends Item> evaluateVariable(XPathContext context) throws XPathException;

    /**
     * Indicate whether the binding is local or global. A global binding is one that has a fixed
     * value for the life of a query or transformation; any other binding is local.
     * @return true if the binding is global
     */

    public boolean isGlobal();

    /**
     * Test whether it is permitted to assign to the variable using the saxon:assign
     * extension element. This will only be for an XSLT global variable where the extra
     * attribute saxon:assignable="yes" is present.
     * @return true if the binding is assignable
    */

    public boolean isAssignable();

    /**
     * If this is a local variable held on the local stack frame, return the corresponding slot number.
     * In other cases, return -1.
     * @return the slot number on the local stack frame
     */

    public int getLocalSlotNumber();

    /**
     * Get the name of the variable
     * @return the name of the variable, as a structured QName
     */

    public StructuredQName getVariableQName();

         /**
     * Register a variable reference that refers to the variable bound in this expression
     * @param isLoopingReference - true if the reference occurs within a loop, such as the predicate
     * of a filter expression
     */

    public void addReference(boolean isLoopingReference);

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