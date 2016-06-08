package jspell;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;

import jspell.modules.ModuleAggregator;

public class Dictionary 
{
	private final HashMap<String, Integer> words;
	private final String name;
	private final ModuleAggregator siblingsSearcher;
	private final Locale locale;
	
	public Dictionary(String name, File f, Locale locale) throws FileNotFoundException
	{
		words = new HashMap<>();
		buildWordsList(f);
		this.name = name;
		this.locale = locale;
		this.siblingsSearcher = new ModuleAggregator(this);
	}
	
	public Dictionary(String name, File f) throws FileNotFoundException
	{
		this(name, f, Locale.US);
	}
	
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
		
		System.out.println(words.size()+" mots chargés.");
	}
	
	public boolean containsWord(String w)
	{
		return words.containsKey(w.toLowerCase());
	}
	
	public String getName() 
	{
		return name;
	}

	public HashMap<String, Integer> getWords() 
	{
		return words;
	}
	
	public Locale getLocale() 
	{
		return locale;
	}

	public double getProbability(String s)
	{
		Integer occurence = words.get(s);
		if(occurence == null) return 0;
		return (double)occurence/(double)words.size();
	}
	
	public void addToDictionary(String s)
	{
		words.put(s, 1);
	}
	
	public String[] getNearestSiblings(String word)
	{
		return siblingsSearcher.getNearestSiblings(word);
	}
	
	
	public static void main(String[] args) throws FileNotFoundException 
	{
		System.out.println("UNIT TEST FOR DICTIONARY");
		
		Dictionary fr = new Dictionary("Francais", new File("dic/francais.txt"), Locale.FRENCH);
		Dictionary en = new Dictionary("English", new File("dic/english.txt"));
		
		Corrector corr = new Corrector(fr, en);
		
		corr.annotateText(new File("shakespeare.txt"), new File("output.txt"));
		corr.annotateText(new File("demain.txt"), new File("output2.txt"));
	}
}
