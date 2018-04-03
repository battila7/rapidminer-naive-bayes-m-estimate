package battila.rapidminer.extension.operator.mestimate;

import static com.rapidminer.example.set.ExampleSetUtilities.SetsCompareOption.ALLOW_SUBSET;
import static com.rapidminer.example.set.ExampleSetUtilities.TypesCompareOption.ALLOW_SAME_PARENTS;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    private final Attribute labelAttribute;

    NaiveBayesModel(ExampleSet trainingExampleSet, double m, Map<Double, Double> presetPriors) {
        super(trainingExampleSet, ALLOW_SUBSET, ALLOW_SAME_PARENTS);

        this.labelAttribute = trainingExampleSet.getAttributes().getLabel();
        this.countPerClass = new HashMap<>();
        this.calculators = new HashMap<>();
        this.exampleCount = trainingExampleSet.size();

        Map<Double, Double> priors = new HashMap<>(presetPriors);

        final Map<String, ProbabilityCalculator.Builder> calculatorBuilders = new HashMap<>();

        Attribute[] regularAttributes = trainingExampleSet.getAttributes().createRegularAttributeArray();

        LogService.getRoot().info("m" + Double.toString(m));

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

        // Note, that this updates the priors used by the NominalProbabilityCalculators.
        // Although, this is quite a bad practice, it's done for efficiency reasons (ie. scan the examples only once).
        for (Map.Entry<Double, Integer> entry : countPerClass.entrySet()) {
            priors.putIfAbsent(entry.getKey(), entry.getValue().doubleValue() / (double)exampleCount);
        }

        LogService.getRoot().info("Priors");
        for (Map.Entry<Double, Double> entry : priors.entrySet()) {
            LogService.getRoot().info(labelAttribute.getMapping().mapIndex(entry.getKey().intValue()));
            LogService.getRoot().info(entry.getValue().toString());
        }

        for (Map.Entry<String, ProbabilityCalculator.Builder> entry : calculatorBuilders.entrySet()) {
            this.calculators.put(entry.getKey(), entry.getValue().build());
        }
    }

    @Override
    public ExampleSet performPrediction(ExampleSet exampleSet, Attribute predictedLabel) throws OperatorException {
        final Attribute[] regularAttributes = exampleSet.getAttributes().createRegularAttributeArray();

        for (Example example : exampleSet) {
            final Prediction prediction = predictExample(example, regularAttributes);

            example.setValue(predictedLabel, prediction.predictedClass);

            prediction.confidenceMap.forEach(example::setConfidence);
        }

        return exampleSet;
    }

    private Prediction predictExample(Example example, Attribute[] regularAttributes) {
        double maxProbability = Double.MIN_VALUE;
        double summedProbability = 0;
        double predictedClass = 0;

        final Map<String, Double> confidenceMap = new HashMap<>();

        for (Map.Entry<Double, Integer> clazzEntry : countPerClass.entrySet()) {
            double clazzProbability = (double)clazzEntry.getValue() / (double)exampleCount;

            for (Attribute attribute : regularAttributes) {
                clazzProbability *= Optional.ofNullable(calculators.get(attribute.getName()))
                        .map(calculator -> calculator.calculateFor(example.getValue(attribute), clazzEntry.getKey()))
                        .orElse(Double.MIN_VALUE);
            }

            summedProbability += clazzProbability;

            confidenceMap.put(labelAttribute.getMapping().mapIndex(clazzEntry.getKey().intValue()), clazzProbability);

            if (clazzProbability > maxProbability) {
                maxProbability = clazzProbability;
                predictedClass = clazzEntry.getKey();
            }
        }

        final double heyCompilerThisOneIsFinal = summedProbability;
        confidenceMap.replaceAll((clazz, probability) -> {
            if (Double.isNaN(probability)) {
                return 0.0;
            } else {
                return probability / heyCompilerThisOneIsFinal;
            }
        });

        return new Prediction(predictedClass, confidenceMap);
    }

    private static final class Prediction {
        private final double predictedClass;

        private final Map<String, Double> confidenceMap;

        private Prediction(double predictedClass, Map<String, Double> confidenceMap) {
            this.predictedClass = predictedClass;
            this.confidenceMap = confidenceMap;
        }
    }
}
