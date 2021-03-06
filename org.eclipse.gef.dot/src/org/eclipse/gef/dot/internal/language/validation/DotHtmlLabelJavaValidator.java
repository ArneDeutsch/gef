/*******************************************************************************
 * Copyright (c) 2017 itemis AG and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 *     Tamas Miklossy   (itemis AG) - minor refactorings
 *     
 *******************************************************************************/
package org.eclipse.gef.dot.internal.language.validation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.gef.dot.internal.language.htmllabel.DotHtmlLabelHelper;
import org.eclipse.gef.dot.internal.language.htmllabel.HtmlAttr;
import org.eclipse.gef.dot.internal.language.htmllabel.HtmlContent;
import org.eclipse.gef.dot.internal.language.htmllabel.HtmlTag;
import org.eclipse.gef.dot.internal.language.htmllabel.HtmllabelPackage;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.validation.Check;

/**
 * This class contains custom validation rules.
 *
 * See
 * https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */
public class DotHtmlLabelJavaValidator extends
		org.eclipse.gef.dot.internal.language.validation.AbstractDotHtmlLabelJavaValidator {

	/**
	 * Checks if the given {@link HtmlTag} is properly closed. Generates errors
	 * if the html's open tag does not correspond to its close tag.
	 * 
	 * @param tag
	 *            The {@link HtmlTag} to check.
	 */
	@Check
	public void checkTagIsClosed(HtmlTag tag) {
		if (!tag.getName().toUpperCase()
				.equals(tag.getCloseName().toUpperCase())) {
			reportRangeBasedError(
					"Tag '<" + tag.getName() + ">' is not closed (expected '</"
							+ tag.getName() + ">' but got '</"
							+ tag.getCloseName() + ">').",
					tag, HtmllabelPackage.Literals.HTML_TAG__CLOSE_NAME);
		}
	}

	/**
	 * Checks if the given {@link HtmlTag} is properly closed. Generates errors
	 * if the html tag is self-closed where self-closing is not allowed.
	 * 
	 * @param tag
	 *            The {@link HtmlTag} to check.
	 */
	@Check
	public void checkSelfClosingTagIsAllowed(HtmlTag tag) {

		String tagNameUpperCase = tag.getName().toUpperCase();

		if (tag.isSelfClosing() && DotHtmlLabelHelper.getNonSelfClosingTags()
				.contains(tagNameUpperCase)) {
			reportRangeBasedError(
					"Tag '<" + tag.getName() + "/>' cannot be self closing.",
					tag, HtmllabelPackage.Literals.HTML_TAG__NAME);
		}
	}

	/**
	 * Checks if a string literal is allowed in the given {@link HtmlTag}.
	 * Generates errors if the html tag is not allowed to contain a string
	 * literal.
	 * 
	 * @param tag
	 *            The {@link HtmlTag} to check.
	 */
	@Check
	public void checkStringLiteralIsAllowed(HtmlTag tag) {

		String[] stringLiteralIsNotAllowed = { "BR", "HR", "IMG", "TABLE", "TR",
				"VR" };

		String tagNameUpperCase = tag.getName().toUpperCase();

		if (Arrays.binarySearch(stringLiteralIsNotAllowed,
				tagNameUpperCase) >= 0) {

			for (HtmlContent child : tag.getChildren()) {
				// TODO: verify why white spaces is stored as text
				String text = child.getText();
				if (text != null && !text.trim().isEmpty()) {
					reportRangeBasedError(
							"Tag '<" + tag.getName()
									+ ">' cannot contain a string literal.",
							tag, HtmllabelPackage.Literals.HTML_TAG__NAME);
				}
			}
		}
	}

	/**
	 * Checks if the given {@link HtmlTag} is valid w.r.t. its parent (not all
	 * tags are allowed on all nesting levels). Generates errors when the given
	 * {@link HtmlTag} is not supported by Graphviz w.r.t. its parent.
	 * 
	 * @param tag
	 *            The {@link HtmlTag} to check.
	 */
	@Check
	public void checkTagNameIsValid(HtmlTag tag) {
		String tagName = tag.getName();
		if (!DotHtmlLabelHelper.getAllTags().contains(tagName.toUpperCase())) {
			reportRangeBasedError("Tag '<" + tagName + ">' is not supported.",
					tag, HtmllabelPackage.Literals.HTML_TAG__NAME);
		} else {
			// find parent tag
			EObject container = tag.eContainer().eContainer();
			HtmlTag parent = null;
			if (container instanceof HtmlTag) {
				parent = (HtmlTag) container;
			}

			// check if tag allowed inside parent or "root" if we could not find
			// a parent
			String parentName = parent == null
					? DotHtmlLabelHelper.getRootTagKey() : parent.getName();
			Map<String, Set<String>> validTags = DotHtmlLabelHelper
					.getValidTags();
			if (!validTags.containsKey(parentName.toUpperCase())
					|| !validTags.get(parentName.toUpperCase())
							.contains(tagName.toUpperCase())) {
				reportRangeBasedError(
						"Tag '<" + tagName + ">' is not allowed inside '<"
								+ parentName + ">', but only inside '<"
								+ String.join(">', '<",
										DotHtmlLabelHelper.getAllowedParents()
												.get(tagName.toUpperCase()))
								+ ">'.",
						tag, HtmllabelPackage.Literals.HTML_TAG__NAME);
			}
		}
	}

	/**
	 * Checks if the given {@link HtmlAttr} is valid w.r.t. its tag (only
	 * certain attributes are supported by the individual tags). Generates
	 * errors if the {@link HtmlAttr} is not supported by Graphviz w.r.t. its
	 * tag.
	 * 
	 * @param attr
	 *            The {@link HtmlAttr} to check.
	 */
	@Check
	public void checkAttributeNameIsValid(HtmlAttr attr) {
		String attrName = attr.getName();
		EObject container = attr.eContainer();
		if (container instanceof HtmlTag) {
			HtmlTag tag = (HtmlTag) container;
			String tagName = tag.getName();
			Map<String, Set<String>> validAttributes = DotHtmlLabelHelper
					.getValidAttributes();
			if (!validAttributes.containsKey(tagName.toUpperCase())
					|| !validAttributes.get(tagName.toUpperCase())
							.contains(attrName.toUpperCase())) {
				reportRangeBasedError(
						"Attribute '" + attrName + "' is not allowed inside '<"
								+ tagName + ">'.",
						attr, HtmllabelPackage.Literals.HTML_ATTR__NAME);
			}
		}
	}

	/**
	 * Checks if the value of a given {@link HtmlAttr} is valid. Generates
	 * errors if the value of a given {@link HtmlAttr} is not supported by
	 * Graphviz.
	 * 
	 * @param attr
	 *            The {@link HtmlAttr} of that's attribute value is to check.
	 */
	@Check
	public void checkAttributeValueIsValid(HtmlAttr attr) {
		String htmlAttributeName = attr.getName();
		// trim the leading and trailing double quotes if necessary
		String htmlAttributeValue = removeDoubleQuotes(attr.getValue());
		EObject container = attr.eContainer();
		if (container instanceof HtmlTag) {
			HtmlTag tag = (HtmlTag) container;
			String htmlTagName = tag.getName();
			String message = getAttributeValueErrorMessage(htmlTagName,
					htmlAttributeName, htmlAttributeValue);
			if (message != null) {
				reportRangeBasedError(
						"The value '" + htmlAttributeValue
								+ "' is not a correct " + htmlAttributeName
								+ ": " + message,
						attr, HtmllabelPackage.Literals.HTML_ATTR__VALUE);
			}
		}
	}

	private String removeDoubleQuotes(String value) {
		if (value.startsWith("\"")) {
			value = value.substring(1);
		}
		if (value.endsWith("\"")) {
			value = value.substring(0, value.length() - 1);
		}
		return value;
	}

	/**
	 * Determines whether the given html attribute value is valid or not.
	 * 
	 * @param htmlTagName
	 *            The html tag name
	 * @param htmlAttributeName
	 *            The html attribute name
	 * @param htmlAttributeValue
	 *            The html attribute value
	 * @return Null if the html attribute is valid, the error message otherwise.
	 */
	private String getAttributeValueErrorMessage(String htmlTagName,
			String htmlAttributeName, String htmlAttributeValue) {
		if ("BR".equalsIgnoreCase(htmlTagName)) { //$NON-NLS-1$
			switch (htmlAttributeName.toUpperCase()) {
			case "ALIGN": //$NON-NLS-1$
				return getEnumAttributeValueErrorMessage(htmlAttributeValue,
						"CENTER", "LEFT", //$NON-NLS-1$
						"RIGHT");
			default:
				break;
			}
		}

		if ("IMG".equalsIgnoreCase(htmlTagName)) { //$NON-NLS-1$
			switch (htmlAttributeName.toUpperCase()) {
			case "SCALE": //$NON-NLS-1$
				return getEnumAttributeValueErrorMessage(htmlAttributeValue,
						"FALSE", "TRUE", //$NON-NLS-1$ //$NON-NLS-2$
						"WIDTH", "HEIGHT", "BOTH"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			default:
				break;
			}
		}

		if ("TABLE".equalsIgnoreCase(htmlTagName)) { //$NON-NLS-1$
			switch (htmlAttributeName.toUpperCase()) {
			case "ALIGN": //$NON-NLS-1$
				return getEnumAttributeValueErrorMessage(htmlAttributeValue,
						"CENTER", "LEFT", //$NON-NLS-1$ //$NON-NLS-2$
						"RIGHT"); //$NON-NLS-1$
			case "BORDER":
				return getNumberAttributeValueErrorMessage(htmlAttributeValue,
						0, 255);
			case "CELLBORDER":
				return getNumberAttributeValueErrorMessage(htmlAttributeValue,
						0, 127);
			case "CELLPADDING":
				return getNumberAttributeValueErrorMessage(htmlAttributeValue,
						0, 255);
			case "CELLSPACING":
				return getNumberAttributeValueErrorMessage(htmlAttributeValue,
						0, 127);
			case "COLUMNS":
			case "ROWS":
				return getEnumAttributeValueErrorMessage(htmlAttributeValue,
						"*");
			case "FIXEDSIZE": //$NON-NLS-1$
				return getEnumAttributeValueErrorMessage(htmlAttributeValue,
						"FALSE", "TRUE"); //$NON-NLS-1$ //$NON-NLS-2$
			case "HEIGHT":
			case "WIDTH":
				return getNumberAttributeValueErrorMessage(htmlAttributeValue,
						0, 65535);
			case "SIDES": //$NON-NLS-1$
				return getSidesAttributeValueErrorMessage(htmlAttributeValue);
			case "VALIGN": //$NON-NLS-1$
				return getEnumAttributeValueErrorMessage(htmlAttributeValue,
						"MIDDLE", "BOTTOM", //$NON-NLS-1$ //$NON-NLS-2$
						"TOP");
			default:
				break;
			}
		}

		if ("TD".equalsIgnoreCase(htmlTagName)) { //$NON-NLS-1$
			switch (htmlAttributeName.toUpperCase()) {
			case "ALIGN": //$NON-NLS-1$
				return getEnumAttributeValueErrorMessage(htmlAttributeValue,
						"CENTER", "LEFT", //$NON-NLS-1$ //$NON-NLS-2$
						"RIGHT", "TEXT"); //$NON-NLS-1$ //$NON-NLS-2$
			case "BALIGN": //$NON-NLS-1$
				return getEnumAttributeValueErrorMessage(htmlAttributeValue,
						"CENTER", "LEFT", //$NON-NLS-1$ //$NON-NLS-2$
						"RIGHT"); //$NON-NLS-1$
			case "BORDER":
				return getNumberAttributeValueErrorMessage(htmlAttributeValue,
						0, 255);
			case "CELLPADDING":
				return getNumberAttributeValueErrorMessage(htmlAttributeValue,
						0, 255);
			case "CELLSPACING":
				return getNumberAttributeValueErrorMessage(htmlAttributeValue,
						0, 127);
			case "COLSPAN":
			case "ROWSPAN":
				return getNumberAttributeValueErrorMessage(htmlAttributeValue,
						1, 65535);
			case "FIXEDSIZE": //$NON-NLS-1$
				return getEnumAttributeValueErrorMessage(htmlAttributeValue,
						"FALSE", "TRUE"); //$NON-NLS-1$ //$NON-NLS-2$
			case "HEIGHT":
			case "WIDTH":
				return getNumberAttributeValueErrorMessage(htmlAttributeValue,
						0, 65535);
			case "SIDES": //$NON-NLS-1$
				return getSidesAttributeValueErrorMessage(htmlAttributeValue);
			case "VALIGN": //$NON-NLS-1$
				return getEnumAttributeValueErrorMessage(htmlAttributeValue,
						"MIDDLE", //$NON-NLS-1$
						"BOTTOM", "TOP"); //$NON-NLS-1$ //$NON-NLS-2$
			default:
				break;
			}
		}

		// html attribute values, that cannot be verified, are considered as
		// valid.
		return null;
	}

	private String getEnumAttributeValueErrorMessage(String currentValue,
			String... allowedValues) {
		List<String> allowedValuesList = Arrays.asList(allowedValues);

		if (allowedValuesList.contains(currentValue.toUpperCase())) {
			return null;
		}

		String formattedAllowedValues = allowedValuesList.stream()
				.map(e -> "'" + e + "'").collect(Collectors.joining(", "));

		return "Value has to be " + (allowedValues.length > 1 ? "one of " : "")
				+ formattedAllowedValues + ".";
	}

	private String getNumberAttributeValueErrorMessage(String currentValue,
			int minimum, int maximum) {
		boolean isValid = true;

		try {
			int currentValueParsed = Integer.parseInt(currentValue);
			isValid = minimum <= currentValueParsed
					&& currentValueParsed <= maximum;
		} catch (NumberFormatException e) {
			isValid = false;
		}

		if (isValid) {
			return null;
		} else {
			return String.format("Value has to be between %1$d and %2$d.",
					minimum, maximum);
		}
	}

	private String getSidesAttributeValueErrorMessage(
			String htmlAttributeValue) {
		if (htmlAttributeValue.isEmpty()) {
			return "Value has to contain only the 'L', 'T', 'R', 'B' characters.";
		}

		for (int i = 0; i < htmlAttributeValue.length(); i++) {
			String subString = Character.toString(htmlAttributeValue.charAt(i))
					.toUpperCase();
			if (!"LTRB".contains(subString)) {
				return "Value has to contain only the 'L', 'T', 'R', 'B' characters.";
			}
		}
		return null;
	}

	private void reportRangeBasedError(String message, EObject object,
			EStructuralFeature feature) {

		List<INode> nodes = NodeModelUtils.findNodesForFeature(object, feature);

		if (nodes.size() != 1) {
			throw new IllegalStateException(
					"Exact 1 node is expected for the feature, but got "
							+ nodes.size() + " node(s).");
		}

		INode node = nodes.get(0);
		int offset = node.getTotalOffset();
		int length = node.getLength();

		String code = null;
		String[] issueData = null;
		getMessageAcceptor().acceptError(message, object, offset, length, code,
				issueData);
	}
}
