package jspell;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
	
	
	private void selectBestDictionary(File f)
	{
		int[] errors = new int[dictionaries.length];
		
		try(Scanner s = new Scanner(f, StandardCharsets.UTF_8.name()))
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
	
	public String[] getNearestSiblings(String word)
	{
		return currentDictionary.getNearestSiblings(word);
	}
	
	public void annotateText(File input, File output)
	{
		System.out.println("Anotating the text \""+input.getName()+"\" and saving it to \""+output.getName()+"\"...");
		selectBestDictionary(input);
		
		try(FileWriter fw = new FileWriter(output); Scanner s = new Scanner(input, StandardCharsets.UTF_8.name()))
		{
			boolean punct =true;
			
			Scanner firstCharTester = new Scanner(input, StandardCharsets.UTF_8.name());
			firstCharTester.useDelimiter("[\\p{Alnum}]");
			String firstToken = firstCharTester.next();
			
			if(firstToken.isEmpty())	punct = false;
			firstCharTester.close();
			
			
			// For each line
			if(punct)
				s.useDelimiter("[\\p{Alnum}]");
			else
				s.useDelimiter("[\\p{Punct}\\p{Space}]+");
			while(s.hasNext())
			{
				String w = s.next();
				
				// Mot
				if(!punct)
				{
					if(currentDictionary.containsWord(w))
					{
						fw.write(w);
					}
					else
					{
						//System.out.println("Word \""+w+"\" mispelled");
						StringBuilder spelling = new StringBuilder("<spell>"+w+"|");
						String[] siblings = getNearestSiblings(w);
						for(String sibling : siblings) spelling.append(sibling).append(",");
						spelling.setLength(spelling.length()-1);
						spelling.append("</spell>");
						fw.write(spelling.toString());
					}
				}
				else
				{
					fw.write(w);
				}
				
				
				punct = !punct;
				if(punct)
					s.useDelimiter("[\\p{Alnum}]");
				else
					s.useDelimiter("[\\p{Punct}\\p{Space}]+");
			}
		}
		catch(IOException e)
		{
			System.err.println("Could not open the input file, or create the output file.");
		}
		
		System.out.println("Done.");
	}
}
