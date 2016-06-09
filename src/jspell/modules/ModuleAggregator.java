package jspell.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import jspell.Dictionary;

public class ModuleAggregator implements Module 
{
	List<Module> modules;
	
	public ModuleAggregator(Dictionary d)
	{
		this.modules = new LinkedList<Module>();
			modules.add(new DamerauLevenshteinModule(d));
			modules.add(new HammingModule(d));
			modules.add(new SoundexModule(d));
	}
	
	@Override
	public String[] getNearestSiblings(String word)
	{
		List<String> words = new ArrayList<>();
		
		for(Module m : modules)
		{
			words.addAll(Arrays.asList(m.getNearestSiblings(word)));
		}
		int max = Math.min(7, words.size());
		return words.subList(0, max).toArray(new String[0]);
	}
	
	public void updateModule(String newWord)
	{
		for(Module m : modules)
		{
			m.updateModule(newWord);
		}
	}
}
