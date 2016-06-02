package jspell;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class Dictionary 
{
	private final HashMap<String, Double> words;
	
	public Dictionary(File f) throws FileNotFoundException
	{
		words = new HashMap<>();
		buildWordsList(f);
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
	
	private double getProbability(String s)
	{
		Double occurence = words.get(s);
		if(occurence == null) return 0;
		return occurence/words.size();
	}
	
	public static void main(String[] args) throws FileNotFoundException 
	{
		System.out.println("UNIT TEST FOR DICTIONARY");
		Dictionary d = new Dictionary(new File("germinal.txt"));
	}
}
