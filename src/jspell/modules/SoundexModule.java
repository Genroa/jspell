package jspell.modules;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jspell.Dictionary;

public class SoundexModule implements Module
{	
	private final Dictionary dictionary;
	private final HashMap<String, Set<String>> soundexWords;
	private char[][] categories;
	
	public SoundexModule(Dictionary d)
	{
		dictionary = d;
		
		buildCategories();
		
		Set<String> wordSet = dictionary.getWords().keySet();
		
		soundexWords = new HashMap<>();
		for(String word : wordSet)
		{
			String soundex = buildSoundex(word);
			Set<String> set = soundexWords.get(soundex);
			if(set == null)
			{
				set = new HashSet<>();
				soundexWords.put(soundex, set);
			}
			set.add(word);
		}
	}
	
	public String buildSoundex(String originalWord)
	{
		char[] word = buildUsableWord(originalWord.toUpperCase());
		char[] soundex = new char[4];
		soundex[0] = word[0];
		for(int i=1; i<4; i++)soundex[i] = '0';
		
		int max = Math.min(word.length, 4);
		
		for(int i=1; i < max; i++) soundex[i] = getNumberRepresentation(word[i]);
		
		String soundexWord = "";
		for(char c : soundex) soundexWord+=c;
		
		return soundexWord;
	}
	
	public char[] buildUsableWord(String originalWord)
	{
		List<Character> list = new ArrayList<>();
		Character[] fc = new Character[] {'A', 'E', 'I', 'O', 'U', 'Y', 'W', 'H'};
		List<Character> forbiddenCharacters = Arrays.asList(fc);
		
		int i=0;
		char lastChar = ' ';
		for(char c : originalWord.toCharArray())
		{
			if((i==0 || !(forbiddenCharacters.contains(c))) && lastChar != c)
			{
				list.add(c);
				lastChar = c;
			}
			i++;
		}
		
		String word = "";
		for(char c : list)
		{
			word += c;
		}
		
		return word.toCharArray();
	}
	
	public char getNumberRepresentation(char c)
	{
		for(int i = 0; i < categories.length; i++)
		{
			char[] letters = categories[i];
			for(char letter : letters)
			{
				if(letter == c)
				{
					char res = (char) (i+1+'0');
					return String.valueOf(res).charAt(0);
				}
			}
		}
		return '0';
	}
	
	@Override
	public String[] getNearestSiblings(String word) 
	{
		String soundex = buildSoundex(word);
		Set<String> set = soundexWords.get(soundex);
		if(set == null) return null;
		
		return set.toArray(new String[0]);
	}
	
	@Override
	public void updateModule(String newWord) 
	{
		String soundex = buildSoundex(newWord);
		Set<String> set = soundexWords.get(soundex);
		if(set == null)
		{
			set = new HashSet<>();
			soundexWords.put(soundex, set);
		}
		set.add(newWord);
	}
	
	public void buildCategories()
	{
		if(dictionary.getLocale() == Locale.FRENCH)
		{
			categories = new char[][]
						{
							{'B', 'F'},
							{'C', 'K', 'Q'},
							{'D', 'T'},
							{'L'},
							{'M', 'N'},
							{'R'},
							{'G', 'J'},
							{'X', 'Z', 'S'},
							{'F', 'V'}
						};
		}
		else
		{
			categories = new char[][]
						{
							{'B', 'F', 'P', 'V'},
							{'C', 'G', 'J', 'K', 'Q', 'S', 'X', 'Z'},
							{'D', 'T'},
							{'L'},
							{'M', 'N'},
							{'R'}
						};
		}
	}
	
	
	public static void main(String[] args) throws FileNotFoundException 
	{
		Dictionary fr = new Dictionary("Francais", new File("dic/francais.txt"), Locale.FRENCH);
		SoundexModule sm = new SoundexModule(fr);
		System.out.println(sm.buildSoundex("bitonnet"));
	}
}
