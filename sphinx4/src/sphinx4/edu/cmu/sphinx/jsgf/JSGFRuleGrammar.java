/**
 * Copyright 1998-2003 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
package edu.cmu.sphinx.jsgf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import edu.cmu.sphinx.jsgf.rule.JSGFRule;
import edu.cmu.sphinx.jsgf.rule.JSGFRuleAlternatives;
import edu.cmu.sphinx.jsgf.rule.JSGFRuleCount;
import edu.cmu.sphinx.jsgf.rule.JSGFRuleName;
import edu.cmu.sphinx.jsgf.rule.JSGFRuleSequence;
import edu.cmu.sphinx.jsgf.rule.JSGFRuleTag;
import edu.cmu.sphinx.jsgf.rule.JSGFRuleToken;

public class JSGFRuleGrammar {

	/** Line delimiter. */
	private static final String LINE_SEPARATOR = System
			.getProperty("line.separator");

	protected final Map<String, JSGFRuleState> rules = new HashMap<String, JSGFRuleState>();
	protected final List<JSGFRuleName> imports = new ArrayList<JSGFRuleName>();
	protected final List<String> importedRules = new ArrayList<String>();

	protected final Map<String, Collection<String>> ruleTags = new HashMap<String, Collection<String>>();

	private String name;
	private JSGFRuleGrammarManager manager;

	/** Storage for documentation comments for rules for JSGF doc. */
	Properties ruleDocComments = new Properties();

	/** Storage for documentation comments for imports for JSGF doc. */
	Properties importDocComments = new Properties();

	/** Storage for documentation comments for the grammar for JSGF doc. */
	String grammarDocComment;

	/* Holds the state of the rule in grammar */
	class JSGFRuleState {
		public boolean isPublic;
		public boolean isEnabled;
		public JSGFRule rule;
		public ArrayList<String> samples;

		public JSGFRuleState (JSGFRule rule,
							  boolean isEnabled, 
							  boolean isPublic) {
			this.rule = rule;
			this.isPublic = isPublic;
			this.isEnabled = isEnabled;
			this.samples = new ArrayList<String>();
		}
	}

	/**
	 * Create a new RuleGrammar
	 * 
	 * @param name
	 *            the name of this Grammar.
	 * @param manager
	 *            the manager for the created Grammars
	 */
	public JSGFRuleGrammar(String name, JSGFRuleGrammarManager manager) {
		this.name = name;
		this.manager = manager;
	}

	/** Add the Grammar comment. */
	public void addGrammarDocComment(String comment) {
		grammarDocComment = comment;
	}

	/**
	 * Import all rules or a specified rule from another grammar.
	 * 
	 * @param importName
	 *            the name of the rule(s) to import.
	 */
	public void addImport(JSGFRuleName importName) {
		if (!imports.contains(importName)) {
			imports.add(importName);
		}
	}

	/** Add a new import comment. */
	public void addImportDocComment(JSGFRuleName imp, String comment) {
		importDocComments.put(imp.toString(), comment);
	}

	/** Add a new RuleGrammar comment. */
	public void addRuleDocComment(String rname, String comment) {
		ruleDocComments.put(rname, comment);
	}

	/**
	 * add a sample sentence to the list of sample sentences that go with the
	 * specified rule
	 */
	public void addSampleSentence(String ruleName, String sample) {
		JSGFRuleState state = rules.get(ruleName);
		if (state == null) {
			return;
		}
		state.samples.add(sample);
	}

	/**
	 * Delete a rule from the grammar.
	 * 
	 * @param ruleName
	 *            the name of the rule.
	 */
	public void deleteRule(String ruleName) throws IllegalArgumentException {
		rules.remove(getKnownRule(ruleName).ruleName);
	}

	/** Retrieve the Grammar comment. */
	public String getGrammarDocComment() {
		return grammarDocComment;
	}

	/** Retrieve an import comment. */
	public String getImportDocComment(JSGFRuleName imp) {
		return importDocComments.getProperty(imp.toString(), null);
	}

	/**
	 * Returns the jsgf tags associated to the given rule. Cf.
	 * jsgf-specification for details.
	 */
	public Collection<String> getJSGFTags(String ruleName) {
		return ruleTags.get(ruleName);
	}

	/**
	 * Gets the Rule with the given name after it has been stripped, or throws
	 * an Exception if it is unknown.
	 */
	private JSGFRule getKnownRule(String ruleName) {
		JSGFRuleState state = rules.get(ruleName);
		if (state == null)
			throw new IllegalArgumentException("Unknown Rule: " + ruleName);
		return state.rule;
	}

	public String getName() {
		return name;
	}

	/**
	 * Return the data structure for the named rule.
	 * 
	 * @param ruleName
	 *            the name of the rule.
	 */
	public JSGFRule getRule(String ruleName) {
		JSGFRuleState state = rules.get(ruleName);
		return state.rule;
	}

	/** Retrieve a RuleGrammar comment. */
	public String getRuleDocComment(String rname) {
		return ruleDocComments.getProperty(rname, null);
	}

	/**
	 * Test whether the specified rule is public.
	 * 
	 * @param ruleName
	 *            the name of the rule.
	 */
	public boolean isRulePublic(String ruleName)
			throws IllegalArgumentException {
		JSGFRuleState state = rules.get(ruleName);
		if (state == null)
			return false;
		return state.isPublic;
	}

	/** List the current imports. */
	public List<JSGFRuleName> getImports () {
		return imports;
	}

	/** List the names of all rules define in this Grammar. */
	public Set<String> getRuleNames () {
		return rules.keySet();
	}

	/**
	 * Remove an import.
	 * 
	 * @param importName
	 *            the name of the rule(s) to remove.
	 */
	public void removeImport(JSGFRuleName importName)
			throws IllegalArgumentException {
		if (imports.contains(importName)) {
			imports.remove(importName);
		}
	}

	/**
	 * Resolve a simple or qualified rule name as a full rule name.
	 * 
	 * @param ruleName
	 *            the name of the rule.
	 */
	public JSGFRuleName resolve(JSGFRuleName ruleName) throws JSGFGrammarException {
		JSGFRuleName rn = new JSGFRuleName(ruleName.getRuleName());

		String simpleName = rn.getSimpleRuleName();
		String grammarName = rn.getSimpleGrammarName();
		String packageName = rn.getPackageName();
		String fullGrammarName = rn.getFullGrammarName();

		// Check for badly formed RuleName
		if (packageName != null && grammarName == null) {
			throw new JSGFGrammarException("Error: badly formed rulename " + rn);
		}

		if (ruleName.getSimpleRuleName().equals("NULL")) {
			return JSGFRuleName.NULL;
		}

		if (ruleName.getSimpleRuleName().equals("VOID")) {
			return JSGFRuleName.VOID;
		}

		// Check simple case: a local rule reference
		if (fullGrammarName == null && this.getRule(simpleName) != null) {
			return new JSGFRuleName(name + '.' + simpleName);
		}

		// Check for fully-qualified reference
		if (fullGrammarName != null) {
			JSGFRuleGrammar g = manager.retrieveGrammar(fullGrammarName);
			if (g != null) {
				if (g.getRule(simpleName) != null) {
					// we have a successful resolution
					return new JSGFRuleName(fullGrammarName + '.' + simpleName);
				}
			}
		}

		// Collect all matching imports into a list. After trying to
		// match rn to each import statement the vec will have
		// size()=0 if rn is unresolvable
		// size()=1 if rn is properly resolvable
		// size()>1 if rn is an ambiguous reference
		List<JSGFRuleName> matches = new ArrayList<JSGFRuleName>();

		// Get list of imports
		// Add local grammar to simply the case of checking for
		// a qualified or fully-qualified local reference.
		List<JSGFRuleName> imports = new ArrayList<JSGFRuleName>(this.imports);
		imports.add(new JSGFRuleName(name + ".*"));

		// Check each import statement for a possible match
		for (JSGFRuleName importName : imports) {
			// TO-DO: update for JSAPI 1.0
			String iSimpleName = importName.getSimpleRuleName();
			String iGrammarName = importName.getSimpleGrammarName();
			String iFullGrammarName = importName.getFullGrammarName();

			// Check for badly formed import name
			if (iFullGrammarName == null)
				throw new JSGFGrammarException("Error: badly formed import "
						+ ruleName);

			// Get the imported grammar
			JSGFRuleGrammar gref = manager.retrieveGrammar(fullGrammarName);
			if (gref == null) {
				System.out.println("Warning: import of unknown grammar "
						+ ruleName + " in " + name);
				continue;
			}

			// If import includes simpleName, test that it really exists
			if (!iSimpleName.equals("*") && gref.getRule(iSimpleName) == null) {
				System.out.println("Warning: import of undefined rule "
						+ ruleName + " in " + name);
				continue;
			}

			// Check for fully-qualified or qualified reference
			if (iFullGrammarName.equals(fullGrammarName)
					|| iGrammarName.equals(fullGrammarName)) {
				// Know that either
				// import <ipkg.igram.???> matches <pkg.gram.???>
				// OR
				// import <ipkg.igram.???> matches <gram.???>
				// (ipkg may be null)

				if (iSimpleName.equals("*")) {
					if (gref.getRule(simpleName) != null) {
						// import <pkg.gram.*> matches <pkg.gram.rulename>
						matches.add(new JSGFRuleName(iFullGrammarName + '.'
								+ simpleName));
					}
					continue;
				} else {
					// Now testing
					// import <ipkg.igram.iRuleName> against <??.gram.ruleName>
					//
					if (iSimpleName.equals(simpleName)) {
						// import <pkg.gram.rulename> exact match for
						// <???.gram.rulename>
						matches.add(new JSGFRuleName(iFullGrammarName + '.'
								+ simpleName));
					}
					continue;
				}
			}

			// If we get here and rn is qualified or fully-qualified
			// then the match failed - try the next import statement
			if (fullGrammarName != null) {
				continue;
			}

			// Now test
			// import <ipkg.igram.*> against <simpleName>

			if (iSimpleName.equals("*")) {
				if (gref.getRule(simpleName) != null) {
					// import <pkg.gram.*> matches <simpleName>
					matches.add(new JSGFRuleName(iFullGrammarName + '.'
							+ simpleName));
				}
				continue;
			}

			// Finally test
			// import <ipkg.igram.iSimpleName> against <simpleName>

			if (iSimpleName.equals(simpleName)) {
				matches.add(new JSGFRuleName(iFullGrammarName + '.' + simpleName));
				continue;
			}
		}

		// The return behavior depends upon number of matches
		switch (matches.size()) {
		case 0: // Return null if rulename is unresolvable
			return null;
		case 1: // Return successfully
			return matches.get(0);
		default: // Throw exception if ambiguous reference
			StringBuilder b = new StringBuilder();
			b.append("Warning: ambiguous reference ").append(rn).append(" in ")
					.append(name).append(" to ");
			for (JSGFRuleName tmp : matches)
				b.append(tmp).append(" and ");
			b.setLength(b.length() - 5);
			throw new JSGFGrammarException(b.toString());
		}
	}

	/** Resolve and link up all rule references contained in all rules. */
	public void resolveAllRules() throws JSGFGrammarException {
		StringBuilder b = new StringBuilder();

		// First make sure that all imports are resolvable
		for (JSGFRuleName ruleName : imports) {
			String grammarName = ruleName.getFullGrammarName();
			JSGFRuleGrammar GI = manager.retrieveGrammar(grammarName);
			if (GI == null) {
				b.append("Undefined grammar ").append(grammarName).append(
						" imported in ").append(name).append('\n');
			}
		}
		if (b.length() > 0) {
			throw new JSGFGrammarException(b.toString());
		}

		for (JSGFRuleState state : rules.values())
			resolveRule(state.rule);
	}

	/** Resolve the given rule. */
	protected void resolveRule(JSGFRule r) throws JSGFGrammarException {

		if (r instanceof JSGFRuleToken) {
			return;
		}

		if (r instanceof JSGFRuleAlternatives) {
			for (JSGFRule rule : ((JSGFRuleAlternatives) r).getRules()) {
				resolveRule(rule);
			}
			return;
		}

		if (r instanceof JSGFRuleSequence) {
			for (JSGFRule rule : ((JSGFRuleSequence) r).getRules()) {
				resolveRule(rule);
			}
			return;
		}

		if (r instanceof JSGFRuleCount) {
			resolveRule(((JSGFRuleCount) r).getRule());
			return;
		}

		if (r instanceof JSGFRuleTag) {
			JSGFRuleTag rt = (JSGFRuleTag) r;

			JSGFRule rule = rt.getRule();
			String ruleStr = rule.toString();

			// add the tag the tag-table
			Collection<String> tags = ruleTags.get(ruleStr);
			if (tags == null) {
				tags = new HashSet<String>();
				ruleTags.put(ruleStr, tags);
			}
			tags.add(rt.getTag());

			resolveRule(rule);
			return;
		}

		if (r instanceof JSGFRuleName) {
			JSGFRuleName rn = (JSGFRuleName) r;
			JSGFRuleName resolved = resolve(rn);

			if (resolved == null) {
				throw new JSGFGrammarException("Unresolvable rulename in grammar "
						+ name + ": " + rn);
			} else {
				// [[[WDW - This forces all rule names to be fully resolved.
				// This should be changed.]]]
				rn.resolvedRuleName = resolved.getRuleName();
				rn.setRuleName(resolved.getRuleName());
				return;
			}
		}

		throw new JSGFGrammarException("Unknown rule type");
	}

	/**
	 * Set the enabled property of the Grammar.
	 * 
	 * @param enabled
	 *            the new desired state of the enabled property.
	 */
	public void setEnabled(boolean enabled) {
		for (JSGFRuleState state : rules.values())
			state.isEnabled = enabled;
	}

	/**
	 * Set the enabled state of the listed rule.
	 * 
	 * @param ruleName
	 *            the name of the rule.
	 * @param enabled
	 *            the new enabled state.
	 */
	public void setEnabled(String ruleName, boolean enabled)
			throws IllegalArgumentException {
		JSGFRuleState state = rules.get(ruleName);
		if (state.isEnabled != enabled) {
			state.isEnabled = enabled;
		}
	}

	/**
	 * Set a rule in the grammar either by creating a new rule or updating an
	 * existing rule.
	 * 
	 * @param ruleName
	 *            the name of the rule.
	 * @param rule
	 *            the definition of the rule.
	 * @param isPublic
	 *            whether this rule is public or not.
	 */
	public void setRule(String ruleName, JSGFRule rule, boolean isPublic)
			throws NullPointerException, IllegalArgumentException {
		JSGFRuleState state = new JSGFRuleState (rule, true, isPublic);
		rules.put(ruleName, state);
	}

	/**
	 * Returns a string containing the specification for this grammar.
	 * 
	 * @return specification for this grammar.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("#JSGF V1.0;").append(LINE_SEPARATOR);
		sb.append(LINE_SEPARATOR);
		sb.append("grammar ").append(name).append(';').append(LINE_SEPARATOR);
		sb.append(LINE_SEPARATOR);
		for (String ruleName : rules.keySet()) {
			JSGFRuleState state = rules.get(ruleName);
			if (state.isPublic) {
				sb.append("public ");
			}
			sb.append('<').append(ruleName).append("> = ").append(state.rule)
					.append(';').append(LINE_SEPARATOR);
		}
		return sb.toString();
	}
}