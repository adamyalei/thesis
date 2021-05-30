package de.metanome.algorithms.dcfinder.helpers;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.results.*;
import de.metanome.backend.constants.Constants;
import de.metanome.backend.helper.InputToGeneratorConverter;
import de.metanome.backend.input.file.DefaultFileInputGenerator;
import de.metanome.backend.result_postprocessing.result_analyzer.*;
import de.metanome.backend.result_postprocessing.result_store.*;
import de.metanome.backend.result_postprocessing.results.*;
import de.metanome.backend.results_db.*;
import de.metanome.backend.results_db.Result;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ResultPostProcessorHelper {

  /**
   * Starting point for the result post processing. The results are extracted from disk, analyzed and
   * hold in memory for further analyses.
   */

    /**
     * Loads the results of an algorithm run from hard disk, analyze them without using the actual
     * data and stores them.
     *
     * @param execution Execution containing the algorithm results file path
     * @throws java.io.IOException if the result file could not be loaded
     * @throws de.metanome.algorithm_integration.AlgorithmConfigurationException if the inputs could not be converted to values
     * @throws de.metanome.algorithm_integration.input.InputGenerationException if no input generator could be created
     * @throws de.metanome.algorithm_integration.input.InputIterationException if the file could not be iterated
     */
    public void extractAndStoreResultsDataIndependent(Execution execution)
        throws IOException, AlgorithmConfigurationException, InputGenerationException,
        InputIterationException, NullPointerException, IndexOutOfBoundsException {
      extractAndStoreResults(execution.getResults(), execution.getInputs(), true);
    }

    /**
     * Loads the results of an algorithm run from hard disk, analyze them using the actual data and
     * stores them.
     *
     * @param execution Execution containing the algorithm results file path
     * @throws java.io.IOException if the result file could not be loaded
     * @throws de.metanome.algorithm_integration.AlgorithmConfigurationException if the inputs could not be converted to values
     * @throws de.metanome.algorithm_integration.input.InputGenerationException if no input generator could be created
     * @throws de.metanome.algorithm_integration.input.InputIterationException if the file could not be iterated
     */
    public void extractAndStoreResultsDataDependent(Execution execution)
        throws IOException, AlgorithmConfigurationException, InputGenerationException,
        InputIterationException, NullPointerException, IndexOutOfBoundsException {
      extractAndStoreResults(execution.getResults(), execution.getInputs(), false);
    }

    /**
     * Loads the results of an algorithm run from hard disk, analyzes them without using the actual
     * data and stores them.
     *
     * @param results the results
     * @param inputs  the inputs used by the algorithm
     * @throws java.io.IOException if the result file could not be loaded
     * @throws de.metanome.algorithm_integration.AlgorithmConfigurationException if the inputs could not be converted to values
     * @throws de.metanome.algorithm_integration.input.InputGenerationException if no input generator could be created
     * @throws de.metanome.algorithm_integration.input.InputIterationException if the file could not be iterated
     */
    public void extractAndStoreResultsDataIndependent(Set<Result> results,
        Collection<Input> inputs)
        throws AlgorithmConfigurationException, InputGenerationException, InputIterationException,
        IOException, NullPointerException, IndexOutOfBoundsException {
      extractAndStoreResults(results, inputs, true);
    }

    /**
     * Loads the results of an algorithm run from hard disk, analyzes them using the actual data and
     * stores them.
     *
     * @param results the results
     * @param inputs  the inputs used by the algorithm
     * @throws java.io.IOException if the result file could not be loaded
     * @throws de.metanome.algorithm_integration.AlgorithmConfigurationException if the inputs could not be converted to values
     * @throws de.metanome.algorithm_integration.input.InputGenerationException if no input generator could be created
     * @throws de.metanome.algorithm_integration.input.InputIterationException if the file could not be iterated
     */
    public void extractAndStoreResultsDataDependent(Set<Result> results,
        Collection<Input> inputs)
        throws AlgorithmConfigurationException, InputGenerationException, InputIterationException,
        IOException, NullPointerException, IndexOutOfBoundsException {
      extractAndStoreResults(results, inputs, false);
    }


    /**
     * Loads the results of an algorithm run from hard disk, analyzes and stores them.
     *
     * @param results         the results
     * @param inputs          the inputs used by the algorithm
     * @param dataIndependent true, if the result analyzes should use the actual data, false
     *                        otherwise
     * @throws java.io.IOException if the result file could not be loaded
     * @throws de.metanome.algorithm_integration.AlgorithmConfigurationException if the inputs could not be converted to values
     * @throws de.metanome.algorithm_integration.input.InputGenerationException if no input generator could be created
     * @throws de.metanome.algorithm_integration.input.InputIterationException if the file could not be iterated
     */
    protected void extractAndStoreResults(Set<Result> results, Collection<Input> inputs,
        boolean dataIndependent)
        throws IOException, AlgorithmConfigurationException, InputGenerationException,
        InputIterationException, NullPointerException, IndexOutOfBoundsException {
      ResultsStoreHolder.clearStores();

      // get input generators
      List<RelationalInputGenerator> inputGenerators = new ArrayList<>();
      for (Input input : inputs) {
        if (input instanceof FileInput) {
          File currFile = new File(input.getName());
          if (currFile.isFile()) {
            inputGenerators.add(InputToGeneratorConverter.convertInput(input));
          } else if (currFile.isDirectory()) {
            File[] filesInDirectory = currFile.listFiles(new FilenameFilter() {
              @Override
              public boolean accept(File file, String name) {
                for (String fileEnding : Constants.ACCEPTED_FILE_ENDINGS_ARRAY) {
                  if (name.endsWith(fileEnding)) {
                    return true;
                  }
                }
                return false;
              }
            });
            for (File file : filesInDirectory) {
              inputGenerators.add(new DefaultFileInputGenerator(file, InputToGeneratorConverter.convertInputToSetting((FileInput) input)));
            }
          }
        } else {
          RelationalInputGenerator relInpGen = InputToGeneratorConverter.convertInput(input);
          if (relInpGen != null) {
            inputGenerators.add(relInpGen);
          }
        }
      }

      // check if a database connection was used
      // if this is true, we can not compute ranking results based on column count etc.,
      // because we do not know which specific column was used for profiling
      boolean usedDatabaseConnection = inputGenerators.contains(null);
      inputGenerators =
          usedDatabaseConnection ? new ArrayList<RelationalInputGenerator>() : inputGenerators;

      for (de.metanome.backend.results_db.Result result : results) {
        String fileName = result.getFileName();
        String resultTypeName = result.getType().getName();

//        analyzeAndStoreResults(fileName, resultTypeName, inputGenerators, dataIndependent);
      }
    }

    /**
     * Reads the results from the given file, analyzes them and stores them in a result store.
     *
     * @param fileName        the file name
     * @param name            the name of the result type
     * @param dataIndependent true, if the result analyzes should use the actual data, false
     *                        otherwise
     * @throws java.io.IOException if the result file could not be loaded
     * @throws de.metanome.algorithm_integration.input.InputGenerationException if no input generator could be created
     * @throws de.metanome.algorithm_integration.input.InputIterationException if the file could not be iterated
     * @return
     */
    public List<DenialConstraintResult> analyzeAndStoreResults(List<DenialConstraint> fileName, String name,
        List<RelationalInputGenerator> inputGenerators,
        boolean dataIndependent)
        throws IOException, InputGenerationException, InputIterationException, AlgorithmConfigurationException,
        NullPointerException, IndexOutOfBoundsException {

      if (name.equals(ResultType.DC.getName())) {
        // read results
//        ResultReader<DenialConstraint> resultReader =
//            new ResultReader<>(ResultType.DC);
//        List<DenialConstraint> denialConstraints = resultReader.readResultsFromFile(fileName);
        List<DenialConstraint> denialConstraints = fileName;
        // analyze results
        ResultAnalyzer<DenialConstraint, DenialConstraintResult>
            resultAnalyzer = new DenialConstraintResultAnalyzer(inputGenerators, dataIndependent);
        List<DenialConstraintResult> rankingResults = resultAnalyzer.analyzeResults(denialConstraints);
        return rankingResults;
        // store results
//        DenialConstraintResultStore resultsStore = new DenialConstraintResultStore();
//        resultsStore.store(rankingResults);
//        ResultsStoreHolder.register(name, resultsStore);

      }
      return null;
    }

}
