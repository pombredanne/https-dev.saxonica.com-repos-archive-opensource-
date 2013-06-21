package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.instruct.ComputedAttribute;
import net.sf.saxon.expr.instruct.Executable;
import net.sf.saxon.expr.instruct.FixedAttribute;
import net.sf.saxon.lib.StandardURIChecker;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.om.*;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

/**
* xsl:attribute element in stylesheet. <br>
*/

public final class XSLAttribute extends XSLLeafNodeConstructor {

    /*@Nullable*/ private Expression attributeName;
    private Expression separator;
    private Expression namespace = null;
    private int validationAction = Validation.PRESERVE;
    private SimpleType schemaType;

    public void prepareAttributes() throws XPathException {

		AttributeCollection atts = getAttributeList();

		String nameAtt = null;
		String namespaceAtt = null;
        String selectAtt = null;
        String separatorAtt = null;
		String validationAtt = null;
		String typeAtt = null;

		for (int a=0; a<atts.getLength(); a++) {
			String f = atts.getQName(a);
			if (f.equals(StandardNames.NAME)) {
        		nameAtt = Whitespace.trim(atts.getValue(a));
        	} else if (f.equals(StandardNames.NAMESPACE)) {
        		namespaceAtt = Whitespace.trim(atts.getValue(a));
        	} else if (f.equals(StandardNames.SELECT)) {
        		selectAtt = atts.getValue(a);
        	} else if (f.equals(StandardNames.SEPARATOR)) {
        		separatorAtt = atts.getValue(a);
        	} else if (f.equals(StandardNames.VALIDATION)) {
        		validationAtt = Whitespace.trim(atts.getValue(a));
        	} else if (f.equals(StandardNames.TYPE)) {
        		typeAtt = Whitespace.trim(atts.getValue(a));
        	} else {
        		checkUnknownAttribute(atts.getNodeName(a));
        	}
        }

        if (nameAtt==null) {
            reportAbsence("name");
            return;
        }
        attributeName = makeAttributeValueTemplate(nameAtt);
        if (attributeName instanceof StringLiteral) {
            if (!getConfiguration().getNameChecker().isQName(((StringLiteral)attributeName).getStringValue())) {
                invalidAttributeName("Attribute name " + Err.wrap(nameAtt) + " is not a valid QName");
            }
            if (nameAtt.equals("xmlns")) {
                if (namespace==null) {
                    invalidAttributeName("Invalid attribute name: xmlns");
                }
            }
            if (nameAtt.startsWith("xmlns:")) {
                if (namespaceAtt == null) {
                    invalidAttributeName("Invalid attribute name: " + Err.wrap(nameAtt));
                } else {
                    // ignore the prefix "xmlns"
                    nameAtt = nameAtt.substring(6);
                    attributeName = new StringLiteral(nameAtt);
                }
            }
        }


        if (namespaceAtt!=null) {
            namespace = makeAttributeValueTemplate(namespaceAtt);
            if (namespace instanceof StringLiteral) {
                if (!StandardURIChecker.getInstance().isValidURI(((StringLiteral)namespace).getStringValue())) {
                    compileError("The value of the namespace attribute must be a valid URI", "XTDE0865");
                }
            }
        }

        if (selectAtt!=null) {
            select = makeExpression(selectAtt);
        }

        if (separatorAtt == null) {
            if (selectAtt == null) {
                separator = new StringLiteral(StringValue.EMPTY_STRING);
            } else {
                separator = new StringLiteral(StringValue.SINGLE_SPACE);
            }
        } else {
            separator = makeAttributeValueTemplate(separatorAtt);
        }

        if (validationAtt!=null) {
            validationAction = Validation.getCode(validationAtt);
            if (validationAction != Validation.STRIP && !getPreparedStylesheet().isSchemaAware()) {
                validationAction = Validation.STRIP;
                compileError("To perform validation, a schema-aware XSLT processor is needed", "XTSE1660");
            }
            if (validationAction == Validation.INVALID) {
                compileError("Invalid value of validation attribute", "XTSE0020");
                validationAction = getContainingStylesheet().getDefaultValidation();
            }
        } else {
            validationAction = getContainingStylesheet().getDefaultValidation();
        }

        if (typeAtt!=null) {
            if (!getPreparedStylesheet().isSchemaAware()) {
                compileError(
                        "The @type attribute is available only with a schema-aware XSLT processor", "XTSE1660");
            } else {
                SchemaType type = getSchemaType(typeAtt);
                if (type == null) {
                    compileError("Unknown attribute type " + typeAtt, "XTSE1520");
                } else {
                    if (type.isSimpleType()) {
                        schemaType = (SimpleType)type;
                    } else {
                        compileError("Type annotation for attributes must be a simple type", "XTSE1530");
                    }
                }
                validationAction = Validation.BY_TYPE;
            }
        }

        if (typeAtt != null && validationAtt != null) {
            compileError("The validation and type attributes are mutually exclusive", "XTSE1505");
            validationAction = getContainingStylesheet().getDefaultValidation();
            schemaType = null;
        }
    }

    private void invalidAttributeName(String message) throws XPathException {
//        if (forwardsCompatibleModeIsEnabled()) {
//            DynamicError err = new XPathException(message);
//            err.setErrorCode("XTDE0850");
//            err.setLocator(this);
//            attributeName = new ErrorExpression(err);
//        } else {
            compileError(message, "XTDE0850");
            // prevent a duplicate error message...
            attributeName = new StringLiteral("saxon-error-attribute");
//        }
    }

    public void validate(Declaration decl) throws XPathException {
        if (schemaType != null) {
            if (schemaType.isNamespaceSensitive()) {
                compileError("Validation at attribute level must not specify a " +
                        "namespace-sensitive type (xs:QName or xs:NOTATION)", "XTTE1545");
            }
        }
        attributeName = typeCheck("name", attributeName);
        namespace = typeCheck("namespace", namespace);
        select = typeCheck("select", select);
        separator = typeCheck("separator", separator);
        super.validate(decl);
    }

    /**
     * Get the error code to be returned when the element has a select attribute but is not empty.
     *
     * @return the error code defined for this condition, for this particular instruction
     */

    protected String getErrorCodeForSelectPlusContent() {
        return "XTSE0840";
    }

    public Expression compile(Executable exec, Declaration decl) throws XPathException {
        NamespaceResolver nsContext = null;

        // deal specially with the case where the attribute name is known statically

        if (attributeName instanceof StringLiteral) {
            String qName = Whitespace.trim(((StringLiteral)attributeName).getStringValue());
            String[] parts;
            try {
                parts = getConfiguration().getNameChecker().getQNameParts(qName);
            } catch (QNameException e) {
                // This can't happen, because of previous checks
                return null;
            }

            if (namespace==null) {
                String nsuri = "";
                if (!parts[0].equals("")) {
                    nsuri = getURIForPrefix(parts[0], false);
                    if (nsuri == null) {
                        undeclaredNamespaceError(parts[0], "XTSE0280");
                        return null;
                    }
                }
                NodeName attributeName = new FingerprintedQName(parts[0], nsuri, parts[1]);
                attributeName.allocateNameCode(getNamePool());
                FixedAttribute inst = new FixedAttribute(attributeName,
                                                         validationAction,
                                                         schemaType);
                inst.setContainer(this);     // temporarily
                compileContent(exec, decl, inst, separator);
                return inst;
            } else if (namespace instanceof StringLiteral) {
                String nsuri = ((StringLiteral)namespace).getStringValue();
                if (nsuri.equals("")) {
                    parts[0] = "";
                } else if (parts[0].equals("")) {
                    // Need to choose an arbitrary prefix
                    // First see if the requested namespace is declared in the stylesheet
                    AxisIterator iter = iterateAxis(Axis.NAMESPACE);
                    while (true) {
                        NodeInfo ns = iter.next();
                        if (ns == null) {
                            break;
                        }
                        if (ns.getStringValue().equals(nsuri)) {
                            parts[0] = ns.getLocalPart();
                            break;
                        }
                    }
                    // Otherwise see the URI is known to the namepool
                    if (parts[0].equals("")) {
                        String p = getNamePool().suggestPrefixForURI(
                                ((StringLiteral)namespace).getStringValue());
                        if (p != null) {
                            parts[0] = p;
                        }
                    }
                    // Otherwise choose something arbitrary. This will get changed
                    // if it clashes with another attribute
                    if (parts[0].equals("")) {
                        parts[0] = "ns0";
                    }
                }
                NodeName nameCode = new FingerprintedQName(parts[0], nsuri, parts[1]);
                nameCode.allocateNameCode(getNamePool());
                FixedAttribute inst = new FixedAttribute(nameCode,
                                                         validationAction,
                                                         schemaType);
                compileContent(exec, decl, inst, separator);
                return inst;
            }
        } else {
            // if the namespace URI must be deduced at run-time from the attribute name
            // prefix, we need to save the namespace context of the instruction

            if (namespace==null) {
                nsContext = makeNamespaceContext();
            }
        }

        ComputedAttribute inst = new ComputedAttribute( attributeName,
                                        namespace,
                                        nsContext,
                                        validationAction,
                                        schemaType,
                                        false);
        compileContent(exec, decl, inst, separator);
        return inst;
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