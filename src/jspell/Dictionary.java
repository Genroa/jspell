package jspell;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class Dictionary 
{
	private final HashMap<String, Double> words;
	private final String name;
	
	public Dictionary(String name, File f) throws FileNotFoundException
	{
		words = new HashMap<>();
		buildWordsList(f);
		this.name = name;
	}
	
	private void buildWordsList(File f) throws FileNotFoundException
	{
		try(Scanner s = new Scanner(f))
		{
			s.useDelimiter("[\\p{Punct}\\p{Space}]");
			while(s.hasNext())
			{
				String w = s.next();
				if(!w.isEmpty())
				{
					w = w.toLowerCase();
					Double occurences = words.get(w);
					if(occurences == null)
					{
						words.put(w, 1.0);
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
		return words.containsKey(w);
	}
	
	public String getName() 
	{
		return name;
	}

	public HashMap<String, Double> getWords() 
	{
		return words;
	}
	
	public double getProbability(String s)
	{
		Double occurence = words.get(s);
		if(occurence == null) return 0;
		return occurence/(double)words.size();
	}
	
	public static void main(String[] args) throws FileNotFoundException 
	{
		System.out.println("UNIT TEST FOR DICTIONARY");
		
		Dictionary fr = new Dictionary("Français", new File("dic/francais.txt"));
		Dictionary en = new Dictionary("English", new File("dic/english.txt"));
		
		Corrector corr = new Corrector(fr, en);
		
		corr.selectBestDictionary(new File("shakespeare.txt"));
		corr.selectBestDictionary(new File("demain.txt"));
	}
}
