package net.sf.saxon.tree.iter;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;

/**
 * This class wraps any sequence iterator, turning it into a lookahead iterator,
 * by looking ahead one item
 */
public class LookaheadIteratorImpl implements LookaheadIterator {

    private SequenceIterator base;
    /*@Nullable*/ private Item current;
    /*@Nullable*/ private Item next;

    private LookaheadIteratorImpl(/*@NotNull*/ SequenceIterator base) throws XPathException {
        this.base = base;
        next = base.next();
    }

    /*@NotNull*/ public static LookaheadIterator makeLookaheadIterator(/*@NotNull*/ SequenceIterator base) throws XPathException {
        if ((base.getProperties() & SequenceIterator.LOOKAHEAD) != 0) {
            return (LookaheadIterator)base;
        } else {
            return new LookaheadIteratorImpl(base);
        }
    }

    public boolean hasNext() {
        return next != null;
    }

    /*@Nullable*/ public Item next() throws XPathException {
        current = next;
        if (next != null) {
            next = base.next();
        }
        return current;
    }

    /*@Nullable*/ public Item current() {
        return current;
    }

    public int position() {
        int p = base.position();
        return (p <= 0 ? p : p-1);
    }

    public void close() {
        base.close();
    }

    /*@NotNull*/
    public SequenceIterator getAnother() throws XPathException {
        return new LookaheadIteratorImpl(base.getAnother());
    }

    public int getProperties() {
        return LOOKAHEAD;
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