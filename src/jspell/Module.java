package jspell;

public interface Module
{
	public String[] getNearestSiblings(String word);
	public void updateModule();
}
