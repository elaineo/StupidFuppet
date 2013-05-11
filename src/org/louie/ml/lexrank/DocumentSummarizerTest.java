package org.louie.ml.lexrank;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * A command line utility which reads in a file full of sentences in a document, one per line,
 * and runs document summarization on them. Currently just dumps out whichever
 * sentences it comes back with.
 */
public class DocumentSummarizerTest {
	public static void main(String[] args) {
		args = new String[1];
		args[0] = "./health_tap_combo.txt";
		
		List<String> sentences = new ArrayList<String>();
		String line = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(args[0]));
			while ((line = br.readLine()) != null) {
				sentences.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		DocumentSummarizer summarizer = new DocumentSummarizer(sentences);
		List<String> results = summarizer.summarize();
		for (String s : results) {
			System.out.println(s + "\n");
		}
	}
}
