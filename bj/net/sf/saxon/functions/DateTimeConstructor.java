package net.sf.saxon.functions;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.DynamicError;
import net.sf.saxon.value.*;


/**
* This class supports the dateTime($date, $time) function
*/

public class DateTimeConstructor extends SystemFunction {

    /**
    * Evaluate the expression
    */

    public Item evaluateItem(XPathContext context) throws XPathException {
        DateValue arg0 = (DateValue)((AtomicValue)argument[0].evaluateItem(context)).getPrimitiveValue();
        TimeValue arg1 = (TimeValue)((AtomicValue)argument[1].evaluateItem(context)).getPrimitiveValue();
        try {
            return new DateTimeValue(arg0, arg1);
        } catch (DynamicError e) {
            if (e.getLocator() == null) {
                e.setLocator(this);
            }
            if (e.getXPathContext() == null) {
                e.setXPathContext(context);
            }
            throw e;
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
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is Michael H. Kay
//
// Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
// Contributor(s): none.
//
