package jspell;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
		/* decoupage des mots en tableau de caract�re */
		char[] s1 = word1.toCharArray();
	    char[] s2 = word2.toCharArray();

	    /* calcul de la plus petite et grande taille */
	    int shorter = Math.min(s1.length, s2.length);
	    int longest = Math.max(s1.length, s2.length);

	    /* calcul à gauche */
	    int leftResult = 0;
	    for (int i=0; i<shorter; i++)
	    {
	        if (s1[i] != s2[i]) leftResult++;
	    }
	    leftResult += longest - shorter;
	    
	    /* calcul � droite */
	    int rightResult = 0;
	    for (int i=0; i<shorter; i++)
	    {
	        if (s1[(s1.length - 1) - i] != s2[(s2.length - 1) - i]) rightResult++;
	    }
	    rightResult += longest - shorter;

	    /* on renvoie la plus petite distance */
	    return Math.min(leftResult, rightResult);
	}
	
	private String averageString(Set<String> set)
	{
		/* sauvegarde des mots pour eviter de reparcourir l'ensemble */
		Map<Integer, String> existingString = new HashMap<>();
		/* compteur de mots et somme de la longueur des mots */
		int nbWord = 0, sumLength = 0;
		/* pour chaque mot */
		for(String word : set)
		{
			nbWord++;
			int wordLength = word.length();
			sumLength+=wordLength;
			/* si il y a pas deja un mot sauvegarder on save */
			if(!existingString.containsKey(wordLength))
				existingString.put(wordLength, word);
		}
		int averageLength = sumLength/nbWord;
		int i = 0;
		String result;
		/* on modifie pas existingString donc sert � v�rifi� qu'il y a un mot */
		while(!existingString.isEmpty())
		{
			if((result = existingString.get(averageLength + i)) != null)
				return result;
			if((result = existingString.get(averageLength - i)) != null)
				return result;
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
		/* Cr�ation d'un ensemble de mots ayant une distance depuis l'origin entre distanceFromOrigin - 2 et + 2*/
		Set<String> set = new HashSet<>();
		for (int count = 0; count <= 2; count++)
		{
			/* cr�ation du set avec le mot d'une distance de count */
			set.addAll(harmmingWords.get(distanceFromOrigin + count));
			set.addAll(harmmingWords.get(distanceFromOrigin - count));
		}
		/* Pour chaque mot du set */
		for(String current : set)
		{
			int distanceFromWord = hammingDistance(word, current);
			/* parcours des mots sauvegardees */
			for(int i = 0; i <= (resultDistance.length - 1); i++)
			{
				/* On regarde si il est plus proche que les mots sauvegard� */
				if(resultDistance[i] == -1 || 
					resultDistance[i] > distanceFromWord || 
					(resultDistance[i] == distanceFromWord && 
						dictionary.getProbability(result[i]) > dictionary.getProbability(current)))
				{
					/* On decale s'il il est plus proche */
					for(int j = 1; j <= (resultDistance.length - 1) - i; j++)
					{
						resultDistance[resultDistance.length - j] = resultDistance[resultDistance.length - 1 - j];
						result[resultDistance.length - j] = result[resultDistance.length - 1 - j];
					}
					
					/* On sauvegarde la nouvelle valeur */
					resultDistance[i] = distanceFromWord;
					result[i] = current;
					break;
				}
			}
		}
		return result;
	}

	public static void main(String[] args) throws FileNotFoundException
	{
		System.out.println("UNITARY TEST HAMMING MODULE");

		Dictionary fr = new Dictionary("Francais", new File("dic/francais.txt"));
		
		HammingModule hm = new HammingModule(fr);
		
		String[] str = hm.getNearestSiblings("praphrase");
		System.out.println(Arrays.toString(str));
		str = hm.getNearestSiblings("alfabet");
		System.out.println(Arrays.toString(str));
		str = hm.getNearestSiblings("propocition");
		System.out.println(Arrays.toString(str));
		str = hm.getNearestSiblings("apelle");
		System.out.println(Arrays.toString(str));
	}
	
	@Override
	public void updateModule(String newWord) 
	{
		Integer distance = hammingDistance(origin, newWord);
		Set<String> set = harmmingWords.get(distance);
		if(set == null)
		{
			set = new HashSet<>();
			harmmingWords.put(distance, set);
		}
		set.add(newWord);
	}
}
