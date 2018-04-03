package battila.rapidminer.extension.operator.mestimate;

import static com.rapidminer.example.set.ExampleSetUtilities.SetsCompareOption.ALLOW_SUBSET;
import static com.rapidminer.example.set.ExampleSetUtilities.TypesCompareOption.ALLOW_SAME_PARENTS;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.tools.LogService;

class NaiveBayesModel extends PredictionModel {
    private final Map<Double, Integer> countPerClass;

    private final Map<String, ProbabilityCalculator> calculators;

    private final int exampleCount;

    NaiveBayesModel(ExampleSet trainingExampleSet, double m, Map<Double, Double> priors) {
        super(trainingExampleSet, ALLOW_SUBSET, ALLOW_SAME_PARENTS);

        final Attribute labelAttribute = trainingExampleSet.getAttributes().getLabel();
        this.countPerClass = new HashMap<>();
        this.calculators = new HashMap<>();
        this.exampleCount = trainingExampleSet.size();

        final Map<String, ProbabilityCalculator.Builder> calculatorBuilders = new HashMap<>();

        Attribute[] regularAttributes = trainingExampleSet.getAttributes().createRegularAttributeArray();

        for (Attribute attribute : regularAttributes) {
            if (attribute.isNominal()) {
                calculatorBuilders.put(attribute.getName(), new NominalProbabilityCalculator.Builder(attribute, m, priors));
            } else {
                calculatorBuilders.put(attribute.getName(), new GaussianProbabilityCalculator.Builder(attribute));
            }
        }

        for (Example example : trainingExampleSet) {
            countPerClass.merge(example.getValue(labelAttribute), 1, Integer::sum);

            for (Attribute attribute : regularAttributes) {
                calculatorBuilders.get(attribute.getName()).add(example);
            }
        }

        for (Map.Entry<String, ProbabilityCalculator.Builder> entry : calculatorBuilders.entrySet()) {
            this.calculators.put(entry.getKey(), entry.getValue().build());
        }
    }

    @Override
    public ExampleSet performPrediction(ExampleSet exampleSet, Attribute predictedLabel) throws OperatorException {
        final Attribute[] regularAttributes = exampleSet.getAttributes().createRegularAttributeArray();

        for (Example example : exampleSet) {
            example.setValue(predictedLabel, predictExample(example, regularAttributes));
        }

        return exampleSet;
    }

    private double predictExample(Example example, Attribute[] regularAttributes) {
        double maxProbability = Double.MIN_VALUE;
        double predictedClass = 0;

        LogService.getRoot().info("------------");

        for (Map.Entry<Double, Integer> clazzEntry : countPerClass.entrySet()) {
            double clazzProbability = (double)clazzEntry.getValue() / (double)exampleCount;
            Logger lg = LogService.getRoot();
            lg.info("  #");

            lg.info("  " + Double.toString(clazzProbability));

            for (Attribute attribute : regularAttributes) {
                clazzProbability *= Optional.ofNullable(calculators.get(attribute.getName()))
                        .map(calculator -> calculator.calculateFor(example.getValue(attribute), clazzEntry.getKey()))
                        .orElse(Double.MIN_VALUE);
            }

            lg.info(" " + Double.toString(clazzProbability));

            if (clazzProbability > maxProbability) {
                maxProbability = clazzProbability;
                predictedClass = clazzEntry.getKey();
            }
        }

        return predictedClass;
    }
}
