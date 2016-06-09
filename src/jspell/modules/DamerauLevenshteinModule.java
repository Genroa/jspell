package jspell.modules;

import java.util.Arrays;
import java.util.Set;

import jspell.Dictionary;

/**
 * This class implements the {@link Module} interface by searching siblings with the Damerau-Levenshtein algorithm.
 */
public class DamerauLevenshteinModule implements Module
{
	private final Dictionary dictionary;
	
	public DamerauLevenshteinModule(Dictionary d)
	{
		dictionary = d;
	}
	
	@Override
	public String[] getNearestSiblings(String word) 
	{
		String[] siblings = new String[3];
		int[] values = new int[3];
		Arrays.fill(values, -1);
		
		Set<String> words = dictionary.getWords().keySet();
		
		for(String w : words)
		{
			int res = calculateDL(w, word);
			for(int i=0; i<values.length; i++)
			{
				if(values[i]==-1 || values[i] > res)
				{
					for(int j=values.length-1; j>i; j--)
					{
						values[j] = values[j-1];
						siblings[j] = siblings[j-1];
					}
					values[i] = res;
					siblings[i] = w;
					break;
				}
			}
		}
		
		return siblings;
	}
	
	private int calculateDL(String w1, String w2)
	{
		int l1 = w1.length();
		int l2 = w2.length();
		int cost;
		
		int[][] values = new int[l1+1][l2+1];
		
		for(int i=0; i<=l1; i++) values[i][0] = i;
		for(int i=0; i<=l2; i++) values[0][i] = i;
		
		for(int i=1; i<=l1; i++)
		{
			for(int j=1; j<=l2; j++)
			{
				if(w1.charAt(i-1) == w2.charAt(j-1)) cost = 0;
				else cost = 1;
				
				int del = values[i-1][j]+1;
                int ins = values[i][j-1]+1;
                int sub = values[i-1][j-1]+cost;
                
				int min = Math.min(del, Math.min(ins, sub));
				
				values[i][j] = min;
				
				
				if((i > 1) && (j > 1) 
				   && (w1.charAt(i-1) == w2.charAt(j-2)) 
				   && (w1.charAt(i-2) == w2.charAt(j-1)))
				{
					values[i][j] = Math.min(values[i][j], values[i-2][j-2]+cost);
				}
			}
		}
        
	    
		return values[l1][l2];
	}
}
