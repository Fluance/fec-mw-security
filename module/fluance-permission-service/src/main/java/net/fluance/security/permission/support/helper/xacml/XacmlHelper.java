/**
 * 
 */
package net.fluance.security.permission.support.helper.xacml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.fluance.commons.xml.XMLUtils;

public class XacmlHelper {

	/**
	 * Loads a policy from file
	 * @param policyFile
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws XPathExpressionException
	 */
	public Policy loadFromFile(File policyFile)
			throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
		if (policyFile == null || !policyFile.isFile()) {
			throw new FileNotFoundException(
					"Could not find file: " + (policyFile == null ? policyFile : policyFile.getAbsolutePath()));
		}
		Document doc = XMLUtils.loadDocument(policyFile);
		Policy policy = new Policy();

		Node policyDoc = doc.getDocumentElement();
		String policyId = XMLUtils.queryString(policyDoc, "/Policy/@PolicyId");
		String policyTargetRegex = XMLUtils.queryString(policyDoc, "/Policy[@PolicyId=\"" + policyId
				+ "\"]/Target/AnyOf/AllOf/Match[@MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-regexp-match\"]/AttributeValue[@DataType=\"http://www.w3.org/2001/XMLSchema#string\"]");

		policy.setId(policyId);
		policy.setApiTargetRegex(policyTargetRegex);

		List<PolicyRule> rules = loadRules(policyDoc);
		policy.setRules(rules);

		return policy;
	}

	/**
	 * 
	 * @param policyDoc
	 * @return
	 * @throws XPathExpressionException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private List<PolicyRule> loadRules(Node policyDoc)
			throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {
		List<PolicyRule> rules = new ArrayList<>();

		String policyId = XMLUtils.queryString(policyDoc, "/Policy/@PolicyId");
		String ruleXpath = "/Policy[@PolicyId=\"" + policyId + "\"]/Rule";
		NodeList ruleNodes = XMLUtils.queryNodeSet(policyDoc, ruleXpath);
		if (ruleNodes != null) {
			for (int i = 0; i < ruleNodes.getLength(); i++) {
				Node currentNode = ruleNodes.item(i);
				if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
					PolicyRule rule = new PolicyRule();
					String ruleId = XMLUtils.queryString(currentNode, "./@RuleId");
					String ruleEffect = XMLUtils.queryString(currentNode, "./@Effect");
					String ruleResourceIdRegex = XMLUtils.queryString(currentNode,
							"./Target/AnyOf/AllOf/Match[AttributeDesignator/@AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\"]/AttributeValue[@DataType=\"http://www.w3.org/2001/XMLSchema#string\"]");
					String actionRegex = XMLUtils.queryString(currentNode,
							"./Target/AnyOf/AllOf/Match[AttributeDesignator/@AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\"]/AttributeValue");
					List<String> roles = loadRoles(currentNode);
					rule.setId(ruleId);
					rule.setEffect(ruleEffect);
					rule.setActionRegex(actionRegex);
					rule.setResourceRegex(ruleResourceIdRegex);
					rule.setRoles(roles);
					rules.add(rule);
				}
			}
		}

		return rules;
	}

	/**
	 * 
	 * @param ruleNode
	 * @return
	 * @throws XPathExpressionException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private List<String> loadRoles(Node ruleNode)
			throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {
		List<String> roles = new ArrayList<>();
		String rolesXpath = "./Condition/Apply/Apply/AttributeValue";
		NodeList roleNodes = XMLUtils.queryNodeSet(ruleNode, rolesXpath);

		for (int i = 0; i < roleNodes.getLength(); i++) {
			Node roleNode = roleNodes.item(i);
			if (roleNode.getNodeType() == Node.ELEMENT_NODE) {
				String role = roleNode.getTextContent();
				roles.add(role);
			}
		}
		return roles;
	}

}
