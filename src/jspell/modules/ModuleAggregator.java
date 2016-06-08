package jspell.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import jspell.Dictionary;

public class ModuleAggregator implements Module 
{
	private final Dictionary dictionary;
	List<Module> modules;
	
	public ModuleAggregator(Dictionary d)
	{
		this.dictionary = d;
		this.modules = new LinkedList<Module>();
			modules.add(new DamerauLevenshteinModule(dictionary));
			modules.add(new HammingModule(dictionary));
			modules.add(new SoundexModule(dictionary));
	}
	
	@Override
	public String[] getNearestSiblings(String word)
	{
		List<String> words = new ArrayList<>();
		for(Module m : modules)
		{
			words.addAll(Arrays.asList(m.getNearestSiblings(word)));
		}
		
		return words.subList(0, 7).toArray(new String[0]);
	}
	
	public void updateModule(String newWord)
	{
		for(Module m : modules)
		{
			m.updateModule(newWord);
		}
	}
}
