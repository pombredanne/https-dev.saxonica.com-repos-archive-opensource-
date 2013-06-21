package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.instruct.Executable;
import net.sf.saxon.expr.sort.SortExpression;
import net.sf.saxon.expr.sort.SortKeyDefinition;
import net.sf.saxon.om.AttributeCollection;
import net.sf.saxon.om.Axis;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.Whitespace;


/**
* Handler for xsl:perform-sort elements in stylesheet (XSLT 2.0). <br>
*/

public class XSLPerformSort extends StyleElement {

    /*@Nullable*/ Expression select = null;

    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction
    */

    public boolean isInstruction() {
        return true;
    }

    /**
     * Determine the type of item returned by this instruction (only relevant if
     * it is an instruction).
     * @return the item type returned
     */

    protected ItemType getReturnedItemType() {
        if (select==null) {
            return getCommonChildItemType();
        } else {
            final TypeHierarchy th = getConfiguration().getTypeHierarchy();
            return select.getItemType(th);
        }
    }

    /**
    * Determine whether this type of element is allowed to contain a template-body
    * @return true: yes, it may contain a template-body
    */

    public boolean mayContainSequenceConstructor() {
        return true;
    }

    /**
     * Specify that xsl:sort is a permitted child
     */

    protected boolean isPermittedChild(StyleElement child) {
        return (child instanceof XSLSort);
    }

    public void prepareAttributes() throws XPathException {

		AttributeCollection atts = getAttributeList();

		String selectAtt = null;

		for (int a=0; a<atts.getLength(); a++) {
			String f = atts.getQName(a);
			if (f.equals(StandardNames.SELECT)) {
        		selectAtt = atts.getValue(a);
        	} else {
        		checkUnknownAttribute(atts.getNodeName(a));
        	}
        }

        if (selectAtt!=null) {
            select = makeExpression(selectAtt);
        }

    }

    public void validate(Declaration decl) throws XPathException {
        checkSortComesFirst(true);

        if (select != null) {
            // if there is a select attribute, check that there are no children other than xsl:sort and xsl:fallback
            AxisIterator kids = iterateAxis(Axis.CHILD);
            while (true) {
                NodeInfo child = (NodeInfo)kids.next();
                if (child == null) {
                    break;
                }
                if (child instanceof XSLSort || child instanceof XSLFallback) {
                    // no action
                } else if (child.getNodeKind() == Type.TEXT && !Whitespace.isWhite(child.getStringValueCS())) {
                        // with xml:space=preserve, white space nodes may still be there
                    compileError("Within xsl:perform-sort, significant text must not appear if there is a select attribute",
                            "XTSE1040");
                } else {
                    ((StyleElement)child).compileError(
                            "Within xsl:perform-sort, child instructions are not allowed if there is a select attribute",
                            "XTSE1040");
                }
            }
        }
        select = typeCheck("select", select);
    }

    public Expression compile(Executable exec, Declaration decl) throws XPathException {
        SortKeyDefinition[] sortKeys = makeSortKeys(decl);
        if (select != null) {
            return new SortExpression(select, sortKeys);
        } else {
            Expression body = compileSequenceConstructor(exec, decl, iterateAxis(Axis.CHILD), true);
            if (body == null) {
                body = Literal.makeEmptySequence();
            }
            try {
                return new SortExpression(makeExpressionVisitor().simplify(body), sortKeys);
            } catch (XPathException e) {
                compileError(e);
                return null;
            }
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