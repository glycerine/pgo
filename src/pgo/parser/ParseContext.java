package pgo.parser;

import java.nio.file.Path;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pgo.lexer.TLAToken;
import pgo.util.SourceLocation;

public class ParseContext {

	private Path filePath;
	private CharSequence chars;
	private int line;
	private int column;
	private int index;
	
	public static class Mark{
		private int markedLine;
		private int markedColumn;
		private int markedIndex;
		
		public Mark(int markedLine, int markedColumn, int markedIndex) {
			this.markedLine = markedLine;
			this.markedColumn = markedColumn;
			this.markedIndex = markedIndex;
		}

		public int getMarkedLine() { return markedLine; }
		public int getMarkedColumn() { return markedColumn; }
		public int getMarkedIndex() { return markedIndex; }
	}
	
	
	public ParseContext(Path filePath, CharSequence chars) {
		this.filePath = filePath;
		this.chars = chars;
		this.line = 0;
		this.column = 0;
		this.index = 0;
	}

	public Mark mark() {
		return new Mark(line, column, index);
	}

	private SourceLocation updateLocation(String match){
		int startLine = line + 1;
		int startColumn = column + 1;
		index += match.length();
		String lf = System.lineSeparator();
		int pos = 0;
		while(true){
			int nextLf = match.indexOf(lf, pos);
			if(nextLf == -1) break;
			++line;
			pos = nextLf + lf.length();
		}
		if(pos == 0){
			// we didn't change line, just advanced the position along the line
			column += match.length();
		}else{
			// we are on a new line, reset column
			column = match.length() - pos;
		}
		return new SourceLocation(filePath, startLine, line + 1, startColumn, column + 1);
	}

	/**
	 * Attempts to match {@code pattern} at the current position, returns either the match result or null on failure.
	 * @param pattern the pattern to match
	 * @return the result of the match, or null on failure
	 */
	public Located<MatchResult> matchPattern(Pattern pattern){
		if(index > chars.length()) return null;
		Matcher m = pattern.matcher(chars);
		m.region(index, chars.length());
		if(m.lookingAt()){
			return new Located<>(
					updateLocation(m.group()),
					m.toMatchResult());
		}else{
			return null;
		}
	}

	public Located<String> matchCharacters(int count){
		if(index + count <= chars.length()){
			String value = chars.subSequence(index, index+count).toString();
			return new Located<>(
					updateLocation(value),
					value);
		}else{
			return null;
		}
	}

	public SourceLocation matchString(String string){
		if(index + string.length() <= chars.length()
				&& chars.subSequence(index, index+string.length()).toString().equals(string)){
			return updateLocation(string);
		}else{
			return null;
		}
	}

	public SourceLocation getSourceLocation(){
		return new SourceLocation(filePath, line+1, line+1, column+1, column+1);
	}

	public boolean isEOF(){
		return index >= chars.length();
	}
	
	public void restore(Mark mark) {
		line = mark.getMarkedLine();
		column = mark.getMarkedColumn();
		index = mark.getMarkedIndex();
	}

}
