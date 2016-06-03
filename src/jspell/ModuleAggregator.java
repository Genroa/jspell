package jspell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ModuleAggregator implements Module 
{
	private final Dictionary dictionary;
	List<Module> modules;
	
	public ModuleAggregator(Dictionary d)
	{
		this.dictionary = d;
		this.modules = new LinkedList<Module>();
			modules.add(new HammingModule(dictionary));
	}
	
	@Override
	public String[] getNearestSiblings(String word)
	{
		List<String> words = new ArrayList<>();
		for(Module m : modules)
		{
			words.addAll(Arrays.asList(m.getNearestSiblings(word)));
		}
		
		return words.toArray(new String[0]);
	}

}
