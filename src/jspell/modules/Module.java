package jspell.modules;

/**
 * Interface representing a siblings searching module. The search algorithm is detailled by all implementing classes.
 *
 */
public interface Module
{
	/**
	 * Returns the nearest siblings. See implementation detail in the classes implementing this interface.
	 * @param word the unknown word
	 * @return the siblings list
	 */
	public String[] getNearestSiblings(String word);
	
	/**
	 * This method is called whenever a new word is added to the dictionary, to update any possible internal data.
	 * @param newWord the new word to know
	 */
	public default void updateModule(String newWord)
	{
		
	}
}