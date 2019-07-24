package com.czj.main;

public class Entity {
	private String word;
	private int start;
	private int end;
	private String type;

	public Entity(String word, int start, int end, String type) {
		// TODO Auto-generated constructor stub
		this.word = word;
		this.start = start;
		this.end = end;
		this.type = type;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "entity [wordString=" + word + ", start=" + start + ", end=" + end + ", type=" + type + "]";
	}

}
