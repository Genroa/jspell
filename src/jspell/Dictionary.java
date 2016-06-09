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

import jspell.modules.ModuleAggregator;

public class Dictionary 
{
	private final HashMap<String, Integer> words;
	private final String name;
	private final File file;
	private final ModuleAggregator siblingsSearcher;
	private final Locale locale;
	
	public Dictionary(String name, File f, Locale locale) throws FileNotFoundException
	{
		words = new HashMap<>();
		buildWordsList(f);
		this.file = f;
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
	
	public String[] getNearestSiblings(String word)
	{
		return siblingsSearcher.getNearestSiblings(word);
	}
}
