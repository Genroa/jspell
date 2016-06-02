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
			s.useDelimiter("\\{Punct}");
		}
	}
}
