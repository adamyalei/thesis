package de.uni_potsdam.hpi.metanome_test_runner.config;

import de.metanome.algorithms.dcfinder.DCFinder;
import java.io.File;
import javax.xml.soap.SAAJResult;

public class Config {

	public enum Algorithm {
		DCFinder
	}
	
	public enum Dataset {
		// set dataset parameters at the end
		DIAGNOSIS, IMMIGRATION, POPULATION,
		ADULT_TESTING, PLANETS, SYMBOLS, SCIENCE, SATELLITES, GAME,
		ASTRONOMICAL, ABALONE, ADULT, BALANCE, BREAST, BRIDGES, CHESS, ECHODIAGRAM, FLIGHT,
		HEPATITIS, HORSE, IRIS, LETTER, NURSERY,
		PETS, NCVOTER_1K, UNIPROD_1K
	}
	
	public Config.Algorithm algorithm;
	public Config.Dataset dataset;

	public int chunkLength = 10000 * 5000;
	public int bufferLength = 5000;
	public double errorThreshold = 0.01d;
	public String approximationDegree = "0.1";
	public String crossColumnStringMinOverlap = "0.3";
	public boolean noCrossColumn = true;
	public long violationsThreshold = 0L;
	public long rsize = 0;

	public String inputDatasetName;
	public String inputFolderPath = "/Users/testing/Desktop/workspace/MetanomeTestRunner/data" + File.separator;
	public String inputFileEnding = ".csv";
	public String inputFileNullString = "";
	public char inputFileSeparator;
	public char inputFileQuotechar = '\"';
	public char inputFileEscape = '\\';
	public int inputFileSkipLines = 0;
	public boolean inputFileStrictQuotes = false;
	public boolean inputFileIgnoreLeadingWhiteSpace = true;
	public boolean inputFileHasHeader = true;
	public boolean inputFileSkipDifferingLines = true; // Skip lines that differ from the dataset's schema
	
	public String measurementsFolderPath = "io" + File.separator + "measurements" + File.separator;
	
	public String statisticsFileName = "statistics.txt";
	public String resultFileName = "results.txt";
	public String rankingResultFileName = "results_ranking.txt";

	public boolean writeResults = true;
	
	public static Config create(String[] args) {
		if (args.length == 0)
			return new Config();
		String approximationDegree = args[2];
		String crossColumnStringMinOverlap = args[3];
		boolean noCrossColumn = Boolean.parseBoolean(args[4]);

		Config.Algorithm algorithm = null;
		String algorithmArg = args[0].toLowerCase();
		for (Config.Algorithm possibleAlgorithm : Config.Algorithm.values())
			if (possibleAlgorithm.name().toLowerCase().equals(algorithmArg))
				algorithm = possibleAlgorithm;
		
		Config.Dataset dataset = null;
		String datasetArg = args[1].toLowerCase();
		for (Config.Dataset possibleDataset : Config.Dataset.values())
			if (possibleDataset.name().toLowerCase().equals(datasetArg))
				dataset = possibleDataset;

		if ((algorithm == null) || (dataset == null))
			wrongArguments();
		
		return new Config(algorithm, dataset, approximationDegree, crossColumnStringMinOverlap, noCrossColumn);
	}
	
	private static void wrongArguments() {
		StringBuilder message = new StringBuilder();
		message.append("\r\nArguments not supported!");
		message.append("\r\nProvide correct values: <algorithm> <dataset>");
		throw new RuntimeException(message.toString());
	}
	
	public Config() {
		this(Algorithm.DCFinder, Dataset.ADULT, "0.1", "0.3", true);
	}

	public Config(Config.Algorithm algorithm, Config.Dataset dataset, String approximationDegree, String crossColumnStringMinOverlap, boolean noCrossColumn) {
		this.algorithm = algorithm;
		this.setDataset(dataset);
		this.approximationDegree = approximationDegree;
		this.crossColumnStringMinOverlap = crossColumnStringMinOverlap;
		this.noCrossColumn = noCrossColumn;
	}

	@Override
	public String toString() {
		return "Config:\r\n\t" +
			"algorithm: " + this.algorithm.name() + "\r\n\t" +
			"dataset: " + this.inputDatasetName + this.inputFileEnding + "\r\n\t"
		+ "approximation degree: " + this.approximationDegree + "\r\n\t" +
				"cross column min overlap: " + this.crossColumnStringMinOverlap + "\r\n\t" +
				"no cross column: " + this.noCrossColumn
				;
	}

	private void setDataset(Config.Dataset dataset) {
		this.dataset = dataset;
		switch (dataset) {
			case DIAGNOSIS:
				this.inputDatasetName = "WIDP-Diagnosis";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case IMMIGRATION:
				this.inputDatasetName = "immigration";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case POPULATION:
				this.inputDatasetName = "population";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case PLANETS:
				this.inputDatasetName = "WDC_planets";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case SYMBOLS:
				this.inputDatasetName = "WDC_symbols";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case SCIENCE:
				this.inputDatasetName = "WDC_science";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case SATELLITES:
				this.inputDatasetName = "WDC_satellites";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case GAME:
				this.inputDatasetName = "WDC_game";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case ASTRONOMICAL:
				this.inputDatasetName = "WDC_astronomical";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case ABALONE:
				this.inputDatasetName = "abalone";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = false;
				break;
			case ADULT:
				this.inputDatasetName = "adult";
				this.inputFileSeparator = ';';
				this.inputFileHasHeader = true;
				break;
			case ADULT_TESTING:
				this.inputDatasetName = "adult_testing";
				this.inputFileSeparator = ';';
				this.inputFileHasHeader = true;
				break;
			case BALANCE:
				this.inputDatasetName = "balance-scale";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = false;
				break;
			case BREAST:
				this.inputDatasetName = "breast-cancer-wisconsin";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = false;
				break;
			case BRIDGES:
				this.inputDatasetName = "bridges";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = false;
				break;
			case CHESS:
				this.inputDatasetName = "chess";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = false;
				break;
			case ECHODIAGRAM:
				this.inputDatasetName = "echocardiogram";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = false;
				break;
			case FLIGHT:
				this.inputDatasetName = "flight_1k";
				this.inputFileSeparator = ';';
				this.inputFileHasHeader = true;
				break;
			case HEPATITIS:
				this.inputDatasetName = "hepatitis";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = false;
				break;
			case HORSE:
				this.inputDatasetName = "horse";
				this.inputFileSeparator = ';';
				this.inputFileHasHeader = false;
				break;
			case IRIS:
				this.inputDatasetName = "iris";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = false;
				break;
			case LETTER:
				this.inputDatasetName = "letter";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = false;
				break;
			case NURSERY:
				this.inputDatasetName = "nursery";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = false;
				break;
			case PETS:
				this.inputDatasetName = "pets";
				this.inputFileSeparator = ';';
				this.inputFileHasHeader = true;
				break;
			case NCVOTER_1K:
				this.inputDatasetName = "ncvoter_1001r_19c";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case UNIPROD_1K:
				this.inputDatasetName = "uniprot_1001r_223c";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
		}
	}
}
