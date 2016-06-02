package jspell;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Corrector 
{
	private final Dictionary[] dictionaries;
	private Dictionary currentDictionary;
	
	
	
	public Corrector(Dictionary... dictionaries)
	{
		if(dictionaries.length == 0) throw new IllegalArgumentException("Provide at least one dictionary to work with!");
		this.dictionaries = dictionaries;
		this.currentDictionary = dictionaries[0];
	}
	
	
	public void selectBestDictionary(File f)
	{
		int[] errors = new int[dictionaries.length];
		
		try(Scanner s = new Scanner(f))
		{
			s.useDelimiter("[\\p{Punct}\\p{Space}]");
			// For each word
			while(s.hasNext())
			{
				String w = s.next();
				if(!w.isEmpty())
				{
					w = w.toLowerCase();
					
					int i=0;
					// For each dictionary, is it in it?
					for(i=0; i < dictionaries.length; i++)
					{
						if(!dictionaries[i].containsWord(w))
						{
							errors[i]++;
						}
					}
				}
			}
		}
		catch(FileNotFoundException e)
		{
			System.err.println("Couldn't find the file");
		}
		
		
		int i=0;
		int maxErrors = errors[0];
		int index = 0;
		
		// Pour chaque nombre d'erreurs dans un dictionnaire
		for(int errorNumber : errors)
		{
			// Si ce dictionnaire a moins de mots inconnus que les pr�c�dents
			if(errorNumber < maxErrors)
			{
				// On le garde
				index = i;
				maxErrors = errorNumber;
			}
			i++;
		}
		
		System.out.println("Best dictionary for the text \""+f.getName()+"\" is "+dictionaries[index].getName()+" (errors nb : "+errors[index]+")");
		this.currentDictionary = dictionaries[index];
	}


	public Dictionary getCurrentDictionary() 
	{
		return currentDictionary;
	}
	
	
}