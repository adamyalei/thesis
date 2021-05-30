package de.uni_potsdam.hpi.metanome_test_runner.mocks;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.Predicate;
import de.metanome.algorithm_integration.configuration.ConfigurationSettingFileInput;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.results.DenialConstraint;
import de.metanome.algorithm_integration.results.Result;
import de.metanome.algorithms.dcfinder.DCFinder;
import de.metanome.algorithms.dcfinder.DCFinder.Identifier;
import de.metanome.backend.input.file.DefaultFileInputGenerator;
import de.metanome.backend.result_postprocessing.helper.TableInformation;
import de.metanome.backend.result_postprocessing.result_ranking.DenialConstraintResultRanking;
import de.metanome.backend.result_postprocessing.results.DenialConstraintResult;
import de.metanome.backend.result_receiver.ResultCache;
import de.uni_potsdam.hpi.metanome_test_runner.config.Config;
import de.uni_potsdam.hpi.metanome_test_runner.utils.FileUtils;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetanomeMock {

	public static void execute(Config conf) {
		try {
			RelationalInputGenerator inputGenerator = new DefaultFileInputGenerator(new ConfigurationSettingFileInput(
					conf.inputFolderPath + conf.inputDatasetName + conf.inputFileEnding, true,
					conf.inputFileSeparator, conf.inputFileQuotechar, conf.inputFileEscape, conf.inputFileStrictQuotes, 
					conf.inputFileIgnoreLeadingWhiteSpace, conf.inputFileSkipLines, conf.inputFileHasHeader, 
					conf.inputFileSkipDifferingLines, conf.inputFileNullString));
			
			ResultCache resultReceiver = new ResultCache("MetanomeMock", getAcceptedColumns(inputGenerator));
			
			DCFinder algorithm = new DCFinder();

			algorithm.setRelationalInputConfigurationValue(Identifier.INPUT_GENERATOR.name(), inputGenerator);
			algorithm.setStringConfigurationValue(Identifier.CROSS_COLUMN_STRING_MIN_OVERLAP.name(), conf.crossColumnStringMinOverlap);
			algorithm.setStringConfigurationValue(Identifier.APPROXIMATION_DEGREE.name(), conf.approximationDegree);
			algorithm.setIntegerConfigurationValue(Identifier.CHUNK_LENGTH.name(), conf.chunkLength);
			algorithm.setIntegerConfigurationValue(Identifier.BUFFER_LENGTH.name(), conf.bufferLength);
			algorithm.setBooleanConfigurationValue(Identifier.NO_CROSS_COLUMN.name(), conf.noCrossColumn);
			algorithm.setResultReceiver(resultReceiver);
			
			long runtime = System.currentTimeMillis();
			algorithm.execute();
			runtime = System.currentTimeMillis() - runtime;

//			// result analyzer
//			List<RelationalInputGenerator> inputGenerators = new ArrayList<>();
//			inputGenerators.add(inputGenerator);

			writeResults(conf, resultReceiver, algorithm, runtime, inputGenerator);

		}
		catch (AlgorithmExecutionException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static List<ColumnIdentifier> getAcceptedColumns(RelationalInputGenerator relationalInputGenerator) throws InputGenerationException, AlgorithmConfigurationException {
		List<ColumnIdentifier> acceptedColumns = new ArrayList<>();
		RelationalInput relationalInput = relationalInputGenerator.generateNewCopy();
		String tableName = relationalInput.relationName();
		for (String columnName : relationalInput.columnNames())
			acceptedColumns.add(new ColumnIdentifier(tableName, columnName));
		return acceptedColumns;
    }
	
	private static void writeResults(Config conf, ResultCache resultReceiver, Object algorithm, long runtime, RelationalInputGenerator inputGenerator)
			throws IOException, AlgorithmConfigurationException, InputGenerationException, InputIterationException {

		if (conf.writeResults) {
			String ts = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-S").format(new Date(System.currentTimeMillis()));
			String outputPath = conf.measurementsFolderPath + conf.inputDatasetName + "_" + algorithm.getClass().getSimpleName() + "_" + ts + File.separator;
			List<Result> results = resultReceiver.fetchNewResults();

			rankingResult(conf, outputPath, results, inputGenerator);

			FileUtils.writeToFile(
					algorithm.toString() + "\r\n\r\n" + conf.toString() + "\r\n\r\n" +
							"Runtime: " + runtime + "\r\n\r\n" + "Results: " + results.size(),
					outputPath + conf.statisticsFileName);

			// write raw result
			FileUtils.writeToFile(format(results), outputPath + conf.resultFileName);
		}
	}

	private static void rankingResult (Config conf, String outputPath, List<Result> results, RelationalInputGenerator inputGenerator)
			throws AlgorithmConfigurationException, InputGenerationException, InputIterationException, IOException {
		//TODO
		// update result with ranking
		/**
		 Ranking results:
		 - DC Result format
		 - ranking
		 - write
		 */
		List<DenialConstraintResult> dcResults = new ArrayList<>();
		for (Result result : results) {
			DenialConstraint dc = (DenialConstraint) result;
			DenialConstraintResult dcResult = new DenialConstraintResult(dc);
			dcResults.add(dcResult);
		}

		String tableName = dcResults.iterator().next().getTableName();

		Map<String, TableInformation> tableInformationMap;
		TableInformation tableInformation = new TableInformation(inputGenerator, false, new BitSet());
		tableInformationMap = new HashMap<>();
		tableInformationMap.put(tableName,tableInformation);

		DenialConstraintResultRanking ranking = new DenialConstraintResultRanking(dcResults, tableInformationMap);
		ranking.calculateDataDependentRankings();

		FileUtils.writeToFile(rankingFormat(dcResults), outputPath + conf.rankingResultFileName);

	}

	private static String format(List<Result> results) {
		StringBuilder builder = new StringBuilder();
		for (Result result : results) {
			DenialConstraint dc = (DenialConstraint) result;
			builder.append(dc.toString() + "\r\n");

		}
		return builder.toString();
	}

	private static String rankingFormat(List<DenialConstraintResult> results) {
		StringBuilder builder = new StringBuilder();
		for (DenialConstraintResult result : results) {
			builder.append(result.getColumnRatio() + "; " + result.getUniquenessRatio() + ";");
			for (Predicate predicate : result.getResult().getPredicates()){
				builder.append( predicate.toString()+ ";");
			}
			builder.append("\r\n");

		}
		return builder.toString();
	}
}
