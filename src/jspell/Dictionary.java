package jspell;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;

import jspell.modules.Module;
import jspell.modules.ModuleAggregator;

/**
 * Class used to represent a Dictionary.
 *
 */
public class Dictionary 
{
	/**
	 * the words contained in the dictionary
	 */
	private final HashMap<String, Integer> words;
	/**
	 * the dictionary name
	 */
	
	private final String name;
	
	/**
	 * the dictionary file
	 */
	private final File file;
	
	/**
	 * the searching module. Each dictionary has at least one searching module. Several searching modules are called when using the {@link ModuleAggregator} module.
	 */
	private final Module siblingsSearcher;
	
	/**
	 * The dictionayr locale
	 */
	private final Locale locale;
	
	/**
	 * Complete constructor for Dictionary.
	 * @param name name of the dictionary
	 * @param f the dictionary file containing the words
	 * @param locale the associated locale
	 * @throws FileNotFoundException if the file isn't found
	 */
	public Dictionary(String name, File f, Locale locale) throws FileNotFoundException
	{
		words = new HashMap<>();
		buildWordsList(f);
		this.file = f;
		this.name = name;
		this.locale = locale;
		this.siblingsSearcher = new ModuleAggregator(this);
	}
	
	/**
	 * Basic constructor for Dictionary. For french dictionary, use the complete constructor with Locale.FRENCH.
	 * @param name name of the dictionary
	 * @param f the dictionary file containing the words
	 * @throws FileNotFoundException if the file isn't found
	 */
	public Dictionary(String name, File f) throws FileNotFoundException
	{
		this(name, f, Locale.US);
	}
	
	/**
	 * Loads the words of the file into the Dictionary
	 * @param f the dictionary file
	 * @throws FileNotFoundException if the file isn't found
	 */
	private void buildWordsList(File f) throws FileNotFoundException
	{
		try(Scanner s = new Scanner(f, StandardCharsets.UTF_8.name()))
		{
			s.useDelimiter("[\\p{Punct}\\p{Space}]");
			while(s.hasNext())
			{
				String w = s.next();
				if(!w.isEmpty())
				{
					w = w.toLowerCase();
					Integer occurences = words.get(w);
					if(occurences == null)
					{
						words.put(w, 1);
					}
					else
					{
						words.put(w, occurences+1);
					}
				}
			}
		}
	}
	
	/**
	 * Checks if the dictionary contains the given word
	 * @param w the word to check
	 * @return true if the Dictionary contains it, false otherwise
	 */
	public boolean containsWord(String w)
	{
		return words.containsKey(w.toLowerCase());
	}
	
	/**
	 * Getter for the dictionary name
	 * @return the dictionary name
	 */
	public String getName() 
	{
		return name;
	}

	/**
	 * Getter for the words list of the dictionary
	 * @return the words contained in the dictionary
	 */
	public HashMap<String, Integer> getWords() 
	{
		return words;
	}
	
	/**
	 * Getter for the dictionary Locale (Locale.US or Locale.FRENCH)
	 * @return the dictionary Locale
	 */
	public Locale getLocale() 
	{
		return locale;
	}
	
	/**
	 * Returns the probability of the given word
	 * @param s the word
	 * @return the probability, or 0 if the word isn't in the dictionary
	 */
	public double getProbability(String s)
	{
		Integer occurence = words.get(s);
		if(occurence == null) return 0;
		return (double)occurence/(double)words.size();
	}
	
	/**
	 * Adds the given word to the Dictionary. The modification is propagated to the dictionary file.
	 * @param word the word to add
	 */
	public void addToDictionary(String word)
	{
		word = word.toLowerCase();
		words.put(word, 1);
		siblingsSearcher.updateModule(word);
		
		try 
		{
		    Files.write(Paths.get(file.getAbsolutePath()), ("\r\n"+word).getBytes(), StandardOpenOption.APPEND);
		}
		catch (IOException e) 
		{
			e.printStackTrace();
			System.err.println("Can't write to the dictionary file "+file.getAbsolutePath());
		}
	}
	
	/**
	 * Searches the nearest words for the given unknown word.
	 * @param word the unknown word
	 * @return an array containing the nearest words.
	 */
	public String[] getNearestSiblings(String word)
	{
		return siblingsSearcher.getNearestSiblings(word);
	}
}
