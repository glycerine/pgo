package pgo.parser;

import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import pcal.AST;
import pcal.IntPair;
import pcal.ParseAlgorithm;
import pcal.PcalCharReaderPgo;
import pcal.PcalParams;
import pcal.TLAtoPCalMapping;
import pcal.exception.ParseAlgorithmException;
import util.ToolIO;

/**
 * The pluscal parser.
 *
 * This class takes a given pluscal file and parses it into the pluscal AST.
 *
 */
public class PcalParser {
	private PcalParser() {}

	public static AST parse(List<String> lines) throws PGoParseException {
		Logger logger = Logger.getLogger("PlusCal Parser");

		if (ToolIO.getMode() == ToolIO.SYSTEM) {
			logger.info("pcal.trans Version " + PcalParams.version + " of " + PcalParams.modDate);
		}

		/*
		 * This method is called in order to make sure, that the parameters are
		 * not sticky because these are could have been initialized by the
		 * previous run
		 */
		PcalParams.resetParams();

		/*********************************************************************
		 * Get and process arguments.
		 *********************************************************************/

		/**
		 * Create the new TLAtoPCalMapping object, call it mapping here and set
		 * PcalParams.tlaPcalMapping to point to it.
		 */
		TLAtoPCalMapping mapping = new TLAtoPCalMapping();
		PcalParams.tlaPcalMapping = mapping;

		/*********************************************************************
		 * Set untabInputVec to be the vector of strings obtained from *
		 * inputVec by replacing tabs with spaces. * * Tabs are date from the
		 * days when memory cost $1 per bit and are a * stupid anachronism. They
		 * should be banned. Although the various * methods taken from TLATeX
		 * should deal with tabs, there are * undoubtedly corner cases that
		 * don't work right. In particular, I * think there's one case where
		 * PcalCharReader.backspace() might be * called to backspace over a tab.
		 * It's easier to simply get rid of * the tabs than to try to make it
		 * work. * * Since the user might be evil enough to prefer tabs, with
		 * tla-file * input, the parts of the output file that are not produced
		 * by the * translator are copied from inputVec, so any tabs the user
		 * wants * are kept. *
		 *********************************************************************/
		Vector<String> untabInputVec = removeTabs(lines);

		/**
		 * Look through the file for PlusCal options. They are put anywhere in
		 * the file (either before or after the module or in a comment) with the
		 * following sequence PlusCal <optional white space> options <optional
		 * white space> ( <options> )
		 *
		 * where <options> has the same format as options on the command line.
		 */
		IntPair searchLoc = new IntPair(0, 0);
		boolean done = false;
		while (!done) {
			try {
				ParseAlgorithm.FindToken("PlusCal", untabInputVec, searchLoc, "");
				String line = ParseAlgorithm.GotoNextNonSpace(untabInputVec, searchLoc);
				String restOfLine = line.substring(searchLoc.two);
				if (restOfLine.startsWith("options")) {
					// The first string after "PlusCal" not starting with a
					// space character is "options"
					if (ParseAlgorithm.NextNonIdChar(restOfLine, 0) == 7) {
						// The "options" should begin an options line
						PcalParams.optionsInFile = true;
						ParseAlgorithm.ProcessOptions(untabInputVec, searchLoc);
						done = true;
					}
				}
			} catch (ParseAlgorithmException e) {
				// The token "PlusCal" not found.
				done = true;
			}
		}

		/*********************************************************************
		 * Set algLine, algCol to the line and column just after the string *
		 * [--]algorithm that begins the algorithm. (These are Java * ordinals,
		 * in which counting begins at 0.) * * Modified by LL on 18 Feb 2006 to
		 * use untabInputVec instead of * inputVec, to correct bug that occurred
		 * when tabs preceded the * "--algorithm". * * For the code to handle
		 * pcal-input, I introduced the use of * IntPair objects to hold <line,
		 * column> Java coordinates (counting * from zero) in a file (or an
		 * image of a file in a String Vector). * For methods that advance
		 * through the file, the IntPair object is * passed as an argument and
		 * is advanced by the method. This is * what I should have been doing
		 * from the start, but I wasn't smart * enough The IntPair curLoc is the
		 * main one used in the part of the * following code that handles
		 * pcal-file input. *
		 *********************************************************************/
		int algLine = 0;
		int algCol = -1;

		// Search for "--algorithm" or "--fair".
		// If found set algLine and algCol right after the last char,
		// set foundBegin true, and set foundFairBegin true iff it
		// was "--fair". Otherwise, set foundBegin false.
		boolean foundBegin = false;
		boolean foundFairBegin = false;
		while ((algLine < untabInputVec.size()) && !foundBegin) {
			String line = untabInputVec.elementAt(algLine);
			algCol = line.indexOf(PcalParams.BeginAlg);
			if (algCol != -1) {
				algCol = algCol + PcalParams.BeginAlg.length();
				foundBegin = true;
			} else {
				algCol = line.indexOf(PcalParams.BeginFairAlg);
				if (algCol != -1) {
					// Found the "--fair". The more structurally nice thing to
					// do here would be to move past the following "algorithm".
					// However, it's easier to pass a parameter to the
					// ParseAlgorithm
					// class's GetAlgorithm method that tells it to go past the
					// "algorithm" token.
					algCol = algCol + PcalParams.BeginFairAlg.length();
					foundBegin = true;
					foundFairBegin = true;

				} else {
					algLine = algLine + 1;
				}
			}

		}

		if (!foundBegin) {
			throw new PGoParseException("Beginning of algorithm string " + PcalParams.BeginAlg + " not found.");
		}

        /*
         * Set the algColumn and algLine fields of the mapping object.
         */
        mapping.algColumn = algCol;
        mapping.algLine = algLine;

		// Get the annotations for PGo
		// Vector<PGoAnnotation> annotations = findPGoAnnotations(untabInputVec);

		/*********************************************************************
		 * Added by LL on 18 Feb 2006 to fix bugs related to handling of *
		 * comments. * * Remove all comments from the algorithm in
		 * untabInputVec, * replacing (* *) comments by spaces to keep the
		 * algorithm tokens * in the same positions for error reporting. *
		 *********************************************************************/
		try {
			ParseAlgorithm.uncomment(untabInputVec, algLine, algCol);
		} catch (ParseAlgorithmException e) {
			throw new PGoParseException(e.getMessage());
		}

		/*********************************************************************
		 * Set reader to a PcalCharReader for the input file (with tabs and *
		 * the previous translation removed), starting right after the *
		 * PcalParams.BeginAlg string. *
		 *********************************************************************/
		PcalCharReaderPgo reader = new PcalCharReaderPgo(untabInputVec, algLine, algCol, lines.size(), 0);

		/*********************************************************************
		 * Set ast to the AST node representing the entire algorithm. *
		 *********************************************************************/
		AST ast;
		try {
			ast = ParseAlgorithm.getAlgorithm(reader, foundFairBegin);
		} catch (ParseAlgorithmException e) {
			throw new PGoParseException(e.getMessage());
		}
		logger.info("Parsing completed.");

		return ast;
	}

	/**
	 * Finds all comments that are pgo annotations PGo annotations are comments
	 * of format "@PGo{<string>}@PGo"
	 *
	 * @param untabInputVec
	 * @return the parsed go annotations
	 * @throws PGoParseException
	 */
	// private Vector<PGoAnnotation> findPGoAnnotations(Vector untabInputVec) throws PGoParseException {
	// 	Vector<PGoAnnotation> annotations = new Vector<PGoAnnotation>();
	// 	// We only parse comments inside the commented PlusCal algorithm block.
	// 	// We also consider the algorithm block not to be a comment block.
	// 	boolean insidePlusCal = false;
	// 	boolean isCommentBlock = false;
	// 	boolean isCommentLine = false;
	// 	boolean isPGo = false;
	// 	StringBuilder sb = null;
	// 	for (int l = 0; l < untabInputVec.size(); l++) {
	// 		String line = (String) untabInputVec.get(l);
	// 		for (int i = 0; i < line.length(); ++i) {
	// 			char c = line.charAt(i);
	// 			if (!isCommentLine && !isCommentBlock) {
	// 				if (c == '(') {
	// 					if (i + 1 < line.length() && line.charAt(i + 1) == '*') {
	// 						if (!insidePlusCal) {
	// 							insidePlusCal = true;
	// 						} else {
	// 							isCommentBlock = true;
	// 						}
	// 					}
	// 				} else if (c == '\\') {
	// 					if (i + 1 < line.length() && line.charAt(i + 1) == '*') {
	// 						isCommentLine = true;
	// 					}
	// 				}
	// 				if (isCommentBlock || isCommentLine) {
	// 					++i;
	// 					while (i + 1 < line.length() && line.charAt(++i) == '*') {
	// 					}
	// 					continue;
	// 				}
	// 			} else if (isPGo) {
	// 				if (c == '}') {
	// 					if (i + 4 < line.length() && line.charAt(i + 1) == '@' && line.charAt(i + 2) == 'P'
	// 							&& line.charAt(i + 3) == 'G' && line.charAt(i + 4) == 'o') {
	// 						isPGo = false;
	// 						i += 4;
	// 						annotations.add(new PGoAnnotation(sb.toString(), l + 1));
	// 						continue;
	// 					}
	// 				} else if (c == '@') {
	// 					if (i + 4 < line.length() && line.charAt(i + 1) == 'P' && line.charAt(i + 2) == 'G'
	// 							&& line.charAt(i + 3) == 'o' && line.charAt(i + 4) == '{') {
	// 						throw new PGoParseException("Opened new annotation before the previous one was closed", l);
	// 					}
	// 				}
	// 				sb.append(c);
	// 			} else if (isCommentBlock || isCommentLine) {
	// 				if (c == '@') {
	// 					if (i + 4 < line.length() && line.charAt(i + 1) == 'P' && line.charAt(i + 2) == 'G'
	// 							&& line.charAt(i + 3) == 'o' && line.charAt(i + 4) == '{') {
	// 						isPGo = true;
	// 						i += 4;
	// 						sb = new StringBuilder();
	// 						continue;
	// 					}
	// 				} else if (c == '}') {
	// 					if (i + 4 < line.length() && line.charAt(i + 1) == '@' && line.charAt(i + 2) == 'P'
	// 							&& line.charAt(i + 3) == 'G' && line.charAt(i + 4) == 'o') {
	// 						throw new PGoParseException("Closed a non-existent annotation block", l);
	// 					}
	// 				} else if (c == '*') {
	// 					if (i + 1 < line.length() && line.charAt(i + 1) == ')') {
	// 						if (isCommentBlock) {
	// 							isCommentBlock = false;
	// 						} else if (insidePlusCal) {
	// 							insidePlusCal = false;
	// 						} else {
	// 							throw new PGoParseException("Mismatched comment block delimiters", l);
	// 						}
	// 						i++;
	// 					}
	// 				}
	// 			}


	// 		}
	// 		if (isCommentLine) {
	// 			isCommentLine = false;
	// 		}
	// 		if (isPGo && !(isCommentBlock || isCommentLine)) {
	// 			throw new PGoParseException(
	// 				"Reached end of comment block before \"}@PGo\" ended annotation block",
	// 				l + 1);
	// 		}
	// 		if (isPGo) {
	// 			sb.append('\n');
	// 		}
	// 	}

	// 	return annotations;
	// }

	/********************************************************************
	 * Returns a string vector obtained from the string vector vec by *
	 * replacing any evil tabs with the appropriate number of spaces, * where
	 * "appropriate" means adding from 1 to 8 spaces in order to * make the next
	 * character fall on a column with Java column * number (counting from 0)
	 * congruent to 0 mod 8. This is what * Emacs does when told to remove tabs,
	 * which makes it good enough * for me. *
	 ********************************************************************/
	private static Vector<String> removeTabs(List<String> lines) {
		Vector<String> newVec = new Vector<>();
		int i = 0;
		for (String oldLine : lines) {
			StringBuilder newline = new StringBuilder();
			int next = 0;
			while (next < oldLine.length()) {
				if (oldLine.charAt(next) == '\t') {
					int toAdd = 8 - (newline.length() % 8);
					while (toAdd > 0) {
						newline.append(" ");
						toAdd = toAdd - 1;
					}
				} else {
					newline.append(oldLine, next, next + 1);
				}
				next = next + 1;
			}
			newVec.addElement(newline.toString());
			i = i + 1;
		}
		return newVec;
	}
}
