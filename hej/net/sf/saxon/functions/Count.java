package net.sf.saxon.functions;

import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;

/**
 * Implementation of the fn:count function
 */
public class Count extends Aggregate {

    public int getImplementationMethod() {
        return super.getImplementationMethod() | WATCH_METHOD;
    }

    /**
     * For an expression that returns an integer or a sequence of integers, get
     * a lower and upper bound on the values of the integers that may be returned, from
     * static analysis. The default implementation returns null, meaning "unknown" or
     * "not applicable". Other implementations return an array of two IntegerValue objects,
     * representing the lower and upper bounds respectively. The values
     * UNBOUNDED_LOWER and UNBOUNDED_UPPER are used by convention to indicate that
     * the value may be arbitrarily large. The values MAX_STRING_LENGTH and MAX_SEQUENCE_LENGTH
     * are used to indicate values limited by the size of a string or the size of a sequence.
     *
     * @return the lower and upper bounds of integer values in the result, or null to indicate
     *         unknown or not applicable.
     */
    @Override
    public IntegerValue[] getIntegerBounds() {
        return new IntegerValue[]{Int64Value.ZERO, MAX_SEQUENCE_LENGTH};
    }

    /**
    * Evaluate the function
    */

    public Item evaluateItem(XPathContext context) throws XPathException {
        SequenceIterator iter = argument[0].iterate(context);
        return new Int64Value(count(iter));
    }

    /**
     * Get the number of items in a sequence identified by a SequenceIterator
     * @param iter The SequenceIterator. This method moves the current position
     * of the supplied iterator; if this isn't safe, make a copy of the iterator
     * first by calling getAnother(). The supplied iterator must be positioned
     * before the first item (there must have been no call on next()).
     * @return the number of items in the underlying sequence
     * @throws net.sf.saxon.trans.XPathException if a failure occurs reading the input sequence
     */

    public static int count(/*@NotNull*/ SequenceIterator iter) throws XPathException {
        if ((iter.getProperties() & SequenceIterator.LAST_POSITION_FINDER) != 0) {
            return ((LastPositionFinder)iter).getLength();
        } else {
            int n = 0;
            while (iter.next() != null) {
                n++;
            }
            return n;
        }
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