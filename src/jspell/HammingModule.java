package jspell;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class HammingModule implements Module
{
	
	private String origin;
	private HashMap<Integer,Set<String>> harmmingWords;
	private Dictionary dictionary;
	
	public HammingModule(Dictionary dictionary)
	{
		this.dictionary = dictionary;
		Set<String> wordSet = dictionary.getWords().keySet();
		this.origin = averageString(wordSet);
		this.harmmingWords = new HashMap<>();
		for(String word : wordSet)
		{
			Integer distance = hammingDistance(origin, word);
			Set<String> set = harmmingWords.get(distance);
			if(set == null)
			{
				set = new HashSet<>();
				harmmingWords.put(distance, set);
			}
			set.add(word);
		}
	}
	
	private int hammingDistance(String word1, String word2)
	{
		char[] s1 = word1.toCharArray();
	    char[] s2 = word2.toCharArray();

	    int shorter = Math.min(s1.length, s2.length);
	    int longest = Math.max(s1.length, s2.length);

	    int result1 = 0;
	    for (int i=0; i<shorter; i++)
	    {
	        if (s1[i] != s2[i]) result1++;
	    }
	    result1 += longest - shorter;
	    
	    int result2 = 0;
	    for (int i=0; i<shorter; i++)
	    {
	        if (s1[(s1.length - 1) - i] != s2[(s2.length - 1) - i]) result2++;
	    }
	    result2 += longest - shorter;

	    return Math.min(result1, result2);
	}
	
	private String averageString(Set<String> set)
	{
		String[] existingString = new String[20];
		int nbWord = 0, sumLength = 0;
		for(String word : set)
		{
			nbWord++;
			int wordLength = word.length();
			sumLength+=wordLength;
			if(wordLength < 20 && existingString[wordLength] == null)
				existingString[wordLength]=word;
		}
		int averageLength = sumLength/nbWord;
		int i = 0;
		System.out.println(averageLength);
		while(averageLength + i < 20 || averageLength - i >= 0)
		{
			if(averageLength + i < 20 && existingString[averageLength + i] != null)
				return existingString[averageLength + i];
			if(averageLength - i >= 0 && existingString[averageLength - i] != null)
				return existingString[averageLength - i];
			i++;
		}
		return null;
	}
	
	@Override
	public String[] getNearestSiblings(String word)
	{
		/* Tableau des resultats */
		String[] result = new String[3];
		/* Tableau de la distance des resultats */
		Integer[] resultDistance = new Integer[3];
		/* -1 pour detecter l'absence d'un mot */
		Arrays.fill(resultDistance, -1);
		
		int distanceFromOrigin = hammingDistance(origin, word);
		/* Pour regarder dans le tampon */
		for (int count = 0; count <= 10; count++)
		{
			/* Parcours des mots d'une distance de s�par� de 2 */
			Set<String> set = new HashSet<>(harmmingWords.get(distanceFromOrigin + count));
			if(distanceFromOrigin - count >= 0)
				set.addAll(harmmingWords.get(distanceFromOrigin - count));
			for(String current : set)
			{
				int distanceFromWord = hammingDistance(word, current);
				/* On regarde si il est plus proche que les mots sauvegard� */
				for(int i = 0; i <= (resultDistance.length - 1); i++)
				{
					if(resultDistance[i] == -1 || 
						resultDistance[i] > distanceFromWord || 
						(resultDistance[i] == distanceFromWord && 
							dictionary.getProbability(result[i]) > dictionary.getProbability(current)))
					{
						/* D�calage */
						for(int j = 1; j <= (resultDistance.length - 1) - i; j++)
						{
							resultDistance[resultDistance.length - j] = resultDistance[resultDistance.length - 1 - j];
							result[resultDistance.length - j] = result[resultDistance.length - 1 - j];
						}
						resultDistance[i] = distanceFromWord;
						result[i] = current;
						break;
					}
				}
			}
		}
		return result;
	}

	public static void main(String[] args) throws FileNotFoundException
	{
		System.out.println("UNITARY TEST HAMMING MODULE");

		Dictionary fr = new Dictionary("Fran�ais", new File("dic/francais.txt"));
		
		HammingModule hm = new HammingModule(fr);
		
		String[] str = hm.getNearestSiblings("praphrase");
		
		System.out.println(Arrays.toString(str));
	}
	
}
