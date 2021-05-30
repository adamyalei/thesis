package de.metanome.algorithms.dcfinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.metanome.algorithms.dcfinder.denialconstraints.DenialConstraintSet;
import de.metanome.algorithms.dcfinder.evidenceset.builders.SplitReconstructEvidenceSetBuilder;
import de.metanome.algorithms.dcfinder.input.Input;
import de.metanome.algorithms.dcfinder.predicates.PredicateBuilder;
import de.metanome.algorithms.dcfinder.setcover.partial.MinimalCoverSearch;

public class DCFinderAlgorithm {
	// TO DELETE
//	protected RelationalInputGenerator inputGenerator = null;
//	protected OrderDependencyResultReceiver resultReceiver = null;
//
//	protected String relationName;
//	protected List<String> columnNames;
//
//	protected String someStringParameter;
//	protected Integer someIntegerParameter;
//	protected Boolean someBooleanParameter;

	
//	public void execute() throws AlgorithmExecutionException {
//
//		////////////////////////////////////////////
//		// THE DISCOVERY ALGORITHM LIVES HERE :-) //
//		////////////////////////////////////////////
//		// Example: Initialize
//		this.initialize();
//		// Example: Read input data
//		List<List<String>> records = this.readInput();
//		// Example: Print what the algorithm read (to test that everything works)
//		this.print(records);
//		// Example: Generate some results (usually, the algorithm should really calculate them on the data)
//		List<OrderDependency> results = this.generateResults();
//		// Example: To test if the algorithm outputs results
//		this.emit(results);
//		/////////////////////////////////////////////
//
//	}
//
//	protected void initialize() throws InputGenerationException, AlgorithmConfigurationException {
//		RelationalInput input = this.inputGenerator.generateNewCopy();
//		this.relationName = input.relationName();
//		this.columnNames = input.columnNames();
//	}
//
//	protected List<List<String>> readInput() throws InputGenerationException, AlgorithmConfigurationException, InputIterationException {
//		List<List<String>> records = new ArrayList<>();
//		RelationalInput input = this.inputGenerator.generateNewCopy();
//		while (input.hasNext())
//			records.add(input.next());
//		return records;
//	}
//
//	protected void print(List<List<String>> records) {
//		// Print parameter
//		System.out.println("Some String: " + this.someStringParameter);
//		System.out.println("Some Integer: " + this.someIntegerParameter);
//		System.out.println("Some Boolean: " + this.someBooleanParameter);
//		System.out.println();
//
//		// Print schema
//		System.out.print(this.relationName + "( ");
//		for (String columnName : this.columnNames)
//			System.out.print(columnName + " ");
//		System.out.println(")");
//
//		// Print records
//		for (List<String> record : records) {
//			System.out.print("| ");
//			for (String value : record)
//				System.out.print(value + " | ");
//			System.out.println();
//		}
//	}
//
//	protected List<OrderDependency> generateResults() {
//		List<OrderDependency> results = new ArrayList<>();
//		ColumnPermutation lhs = new ColumnPermutation(this.getRandomColumn(), this.getRandomColumn());
//		ColumnPermutation rhs = new ColumnPermutation(this.getRandomColumn(), this.getRandomColumn());
//		OrderDependency od = new OrderDependency(lhs, rhs, OrderDependency.OrderType.LEXICOGRAPHICAL, OrderDependency.ComparisonOperator.STRICTLY_SMALLER);
//		results.add(od);
//		return results;
//	}
//
//	protected ColumnIdentifier getRandomColumn() {
//		Random random = new Random(System.currentTimeMillis());
//		return new ColumnIdentifier(this.relationName, this.columnNames.get(random.nextInt(this.columnNames.size())));
//	}
//
//	protected void emit(List<OrderDependency> results) throws CouldNotReceiveResultException, ColumnNameMismatchException {
//		for (OrderDependency od : results)
//			this.resultReceiver.receiveResult(od);
//	}
//
//	@Override
//	public String toString() {
//		return this.getClass().getName();
//	}
protected long chunkLength = 10000 * 5000;
	protected int bufferLength = 5000;
	protected double errorThreshold = 0.01d;
	protected long violationsThreshold = 0L;
	protected long rsize = 0;

	public DenialConstraintSet run(Input input, PredicateBuilder predicates) {

		input.buildPLIs();
		rsize = input.getLineCount();

		setViolationsThreshold();

		SplitReconstructEvidenceSetBuilder evidenceSetBuilder = new SplitReconstructEvidenceSetBuilder(input,
				predicates, chunkLength, bufferLength);
		evidenceSetBuilder.buildEvidenceSet();

		DenialConstraintSet dcs = new MinimalCoverSearch(predicates.getPredicates(), violationsThreshold)
				.getDenialConstraints(evidenceSetBuilder.getFullEvidenceSet());

		return dcs;
	}

	private void setViolationsThreshold() {
		long totaltps = rsize * (rsize - 1);
		violationsThreshold = (long) Math.ceil(((double) totaltps * errorThreshold));
		log.info("Error threshold: " + errorThreshold + ".");
		log.info("Discovering DCs with at most " + violationsThreshold + " violating tuple pairs.");
	}

	private static Logger log = LoggerFactory.getLogger(DCFinder_backup.class);
}
