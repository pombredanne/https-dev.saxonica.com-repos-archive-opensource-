package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.QNameValue;

/**
* This class supports the name(), local-name(), and namespace-uri() functions
* from XPath 1.0, and also the XSLT generate-id() function
*/

public class NodeNameFn extends SystemFunction {

    /**
    * Simplify and validate.
     * @param visitor an expression visitor
     */

     /*@NotNull*/
     public Expression simplify(ExpressionVisitor visitor) throws XPathException {
        useContextItemAsDefault(visitor);
        return simplifyArguments(visitor);
    }

    /**
    * Evaluate the function
    */

    /*@Nullable*/ public Item evaluateItem(XPathContext c) throws XPathException {
        NodeInfo node = (NodeInfo)argument[0].evaluateItem(c);
        if (node==null) {
            return null;
        }
        int nc = node.getNameCode();
        if (nc == -1) {
            return null;
        }
        return new QNameValue(node.getNamePool(), nc);
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