package jspell;

public interface Module
{
	public String[] getNearestSiblings(String word);
	public default void updateModule(String newWord)
	{
		
	}
}