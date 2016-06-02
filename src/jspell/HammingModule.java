package jspell;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class HammingModule implements Module
{
	
	private String origin;
	private HashMap<Integer,Set<String>> harmmingWords;
	
	public HammingModule(Set<String> dictionaryWords)
	{
		origin = averageString(dictionaryWords);
		for(String s : dictionaryWords)
		{
			Integer distance = hammingDistance(s);
			Set<String> set = harmmingWords.get(distance);
			if(set == null)
			{
				set = new HashSet<String>();
				harmmingWords.put(distance, set);
			}
			set.add(s);
		}
	}
	
	private int hammingDistance(String s){
		char[] s1 = origin.toCharArray();
	    char[] s2 = s.toCharArray();

	    int shorter = Math.min(s1.length, s2.length);
	    int longest = Math.max(s1.length, s2.length);

	    int result = 0;
	    for (int i=0; i<shorter; i++) {
	        if (s1[i] != s2[i]) result++;
	    }

	    result += longest - shorter;

	    return result;
	}
	
	private String averageString(Set<String> s)
	{
		String[] existingString = new String[10];
		int nbWord = 0, sumLength = 0;
		for(String word : s)
		{
			nbWord++;
			int wordLength = word.length();
			sumLength+=wordLength;
			if(wordLength < 10 && existingString[wordLength] == null)
				existingString[wordLength]=word;
		}
		int averageLength = sumLength/nbWord;
		int i = 0;
		while(averageLength + i < 10 || averageLength - i >= 0)
		{
			if(averageLength + i < 10 && existingString[averageLength + i] != null)
				return existingString[averageLength + i];
			if(averageLength - i >= 0 && existingString[averageLength - i] != null)
				return existingString[averageLength - i];
			i++;
		}
		return null;
	}

	@Override
	public String[] getNearestSiblings() {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) {
		System.out.println("UNITARY TEST HAMMING MODULE (EMPTY)");
		
	}
	
}
