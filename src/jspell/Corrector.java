package jspell;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Main class, building anotations and running corrections.
 * 
 *
 */
public class Corrector 
{
	/**
	 * The existing dictionaries
	 */
	private final Dictionary[] dictionaries;
	
	/**
	 * The currently used dictionary
	 */
	private Dictionary currentDictionary;
	
	/**
	 * Constructor for the Corrector object.
	 * @param dictionaries the dictionaries the Corrector will be able to use
	 */
	public Corrector(Dictionary... dictionaries)
	{
		if(dictionaries.length == 0) throw new IllegalArgumentException("Provide at least one dictionary to work with!");
		this.dictionaries = dictionaries;
		this.currentDictionary = dictionaries[0];
	}
	
	/**
	 * Selects the best dictionary for the providen text file, and sets it as the currently used dictionary for the Corrector
	 * @param f the text file to analyse
	 */
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

	/**
	 * Getter for the currently used dictionary.
	 * @return the currently used dictionary
	 */
	public Dictionary getCurrentDictionary() 
	{
		return currentDictionary;
	}
	
	/**
	 * Returns the nearest words from the providen word.
	 * @param word the unknown word
	 * @return an array containing the siblings found from the word
	 */
	public String[] getNearestSiblings(String word)
	{
		return currentDictionary.getNearestSiblings(word);
	}
	
	/**
	 * Anotates a text with the propositions for each unknown word (provided by getNearestSiblings).
	 * @param input the input text file to anotate
	 * @param output the name of the anotated text file
	 */
	private void annotateText(File input, File output)
	{
		System.out.println("Anotating the text \""+input.getAbsolutePath()+"\" and saving it to \""+output.getAbsolutePath()+"\"...");
		
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
	
	
	/**
	 * Opens an anotated file and launches the correction process
	 * @param f the anotated file
	 */
	public void correctFile(File f)
	{
		File anotatedText = new File(f.getAbsolutePath()+".anot");
		List<String> customWords = new LinkedList<>();
		
		selectBestDictionary(f);
		annotateText(f, anotatedText);
		
		try(Scanner s = new Scanner(anotatedText, StandardCharsets.UTF_8.name()); 
			Scanner input = new Scanner(System.in);
			FileWriter fw = new FileWriter(f.getAbsolutePath()+".tmp", true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw))
		{
			while(s.hasNextLine())
			{
				String line = s.nextLine();
				List<List<String>> errors = new LinkedList<List<String>>();
				
				String displayLine = buildDisplayLine(line, errors, customWords);
				
				displayLine = correctLine(displayLine, errors, input, customWords);

				out.println(displayLine);
				out.flush();
			}
		} 
		catch (FileNotFoundException e)
		{
			System.out.println("Could not open the anotated text!");
		} 
		catch (IOException e1) 
		{
			
		}
		
		f.delete();
		anotatedText.delete();
		File outputFile = new File(f.getAbsolutePath()+".tmp");
		outputFile.renameTo(f);
		
		ScreenUtils.clearScreen();
		System.out.println("Correction du fichier terminée.");
	}
	
	/**
	 * Returns the choice entered by the user
	 * @param input the Scanner used to read the input
	 * @return the choice
	 */
	private int getChoice(Scanner input)
	{
		int choice = -1;
		
		while(choice < 0 || choice > 9)
		{
			System.out.println("Enter your choice : ");
			
			try
			{
				choice = input.nextInt();
			}
			catch(InputMismatchException e)
			{
				System.out.println("Please enter a choice between 0 and 9.");
				choice = -1;
			}
			input.nextLine();
		}
		return choice;
	}
	
	/**
	 * Corrects a line, error by error.
	 * @param displayLine the formatted line to display
	 * @param errors a list containing for each error the list of propositions
	 * @param input the scanner input to read user's answers
	 * @param customWords the list of the ignored words for this session
	 * @return the corrected line
	 */
	private String correctLine(String displayLine, List<List<String>> errors, Scanner input, List<String> customWords)
	{
		for(List<String> words : errors)
		{
			if(customWords.contains(words.get(0).toLowerCase()) || currentDictionary.containsWord(words.get(0)))
			{
				continue;
			}
			
			displayPropositions(displayLine, words);
			int choice = getChoice(input);
			if(choice < words.size()-1)
			{
				displayLine = displayLine.replaceFirst("\\*\\*"+words.get(0)+"\\*\\*", words.get(choice+1));
			}
			// Ignore
			else if(choice == words.size()-1)
			{
				displayLine = displayLine.replaceFirst("\\*\\*"+words.get(0)+"\\*\\*", words.get(0));
			}
			// Ignore all
			else if(choice == words.size())
			{
				displayLine = displayLine.replaceAll("\\*\\*"+words.get(0)+"\\*\\*", words.get(0));
				displayLine = displayLine.replaceAll("\\*\\*"+words.get(0).toLowerCase()+"\\*\\*", words.get(0).toLowerCase());
				customWords.add(words.get(0).toLowerCase());
			}
			// Add to dictionary
			else if(choice == words.size()+1)
			{
				displayLine = displayLine.replaceAll("\\*\\*"+words.get(0)+"\\*\\*", words.get(0));
				displayLine = displayLine.replaceAll("\\*\\*"+words.get(0).toLowerCase()+"\\*\\*", words.get(0).toLowerCase());
				currentDictionary.addToDictionary(words.get(0));
			}
		}
		
		return displayLine;
	}
	
	/**
	 * Displays the correction menu
	 * @param displayLine the formatted line to display
	 * @param words the list words to ignore for this session
	 */
	private void displayPropositions(String displayLine, List<String> words)
	{
		ScreenUtils.clearScreen();
		System.out.println(displayLine);
		String error = words.get(0);
		System.out.println("Word : "+error);
		System.out.println("Propositions :");
		int i = 0;
		for(String word : words)
		{
			if(i==0) 
			{
				i++;
				continue;
			}
			System.out.println(i-1+" - replace with "+word);
			i++;
		}
		System.out.println(i-1+" - ignore this word"); i++;
		System.out.println(i-1+" - ignore all occurences this word"); i++;
		System.out.println(i-1+" - add \""+error+"\" to dictionary");
	}
	
	/**
	 * Builds the formatted line to display from the line read in the anotated file
	 * @param line the original line
	 * @param errors a list containing for each error the list of propositions
	 * @param customWords the list of the ignored words for this session
	 * @return the formatted string
	 */
	private String buildDisplayLine(String line, List<List<String>> errors, List<String> customWords)
	{
		String displayLine = "";
		
		int firstOc;
		int lastIndex = 0;
		do
		{
			firstOc = line.indexOf("<spell>", lastIndex);
			
			// Si on a trouvé une occurence
			if(firstOc != -1)
			{
				// Avant l'occurence
				displayLine+= line.substring(lastIndex, firstOc);
				// Le mot dans <spell>
				lastIndex = firstOc+"<spell>".length();
				int endOfWord = line.indexOf("|", lastIndex);
				
				String word = line.substring(lastIndex, endOfWord);
				
				if(!customWords.contains(word.toLowerCase()) && !currentDictionary.containsWord(word))
				{
					List<String> words = new ArrayList<String>();
					words.add(word);
					words.addAll(Arrays.asList(line.substring(endOfWord+1, line.indexOf("</spell>", endOfWord+1)).split(",")));
					errors.add(words);
					
					displayLine+= "**"+word+"**";
				}
				else
				{
					displayLine+=word;
				}
				
				
				lastIndex = line.indexOf("</spell>", endOfWord)+"</spell>".length();
				
			}
			else
			{
				displayLine+= line.substring(lastIndex);
				break;
			}
		}
		while(firstOc != -1);
		
		return displayLine;
	}
	
	/**
	 * Main method of the program. 
	 * @param args Arguments of the program
	 * @throws FileNotFoundException if the file isn't found
	 */
	public static void main(String[] args) throws FileNotFoundException 
	{		
		Dictionary fr = new Dictionary("Francais", new File("dic/francais.txt"), Locale.FRENCH);
		Dictionary en = new Dictionary("English", new File("dic/english.txt"));
		
		Corrector corr = new Corrector(fr, en);
		
		if(args.length == 0)
			throw new IllegalArgumentException("The program at least one argument");

		File f = new File(args[0]);
		if(!f.exists()) { 
		    System.err.println("The file " + f.getAbsolutePath() + " doesn't exist or it's a directory.");
		    return;
		}
		corr.correctFile(f);
	}
}
