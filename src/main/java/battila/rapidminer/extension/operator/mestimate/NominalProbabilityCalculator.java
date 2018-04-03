package battila.rapidminer.extension.operator.mestimate;

import java.util.HashMap;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;

final class NominalProbabilityCalculator implements ProbabilityCalculator {
    private final double m;

    private final Map<Double, Double> priors;

    private final Map<Double, Map<Double, Integer>> valueCountPerClass;

    private final Map<Double, Integer> countPerClass;

    private NominalProbabilityCalculator(Builder builder) {
        this.m = builder.m;
        this.priors = builder.priors;
        this.valueCountPerClass =  builder.valueCountPerClass;
        this.countPerClass = builder.countPerClass;
    }

    @Override
    public double calculateFor(double attributeValue, double clazz) {
        final int count = valueCountPerClass.get(attributeValue).get(clazz);

        final double numerator = count /* + m * priors.get(clazz) */;

        return numerator / (countPerClass.get(clazz) /* + m */);
    }

    static final class Builder implements ProbabilityCalculator.Builder {
        private final Attribute targetAttribute;

        private final double m;

        private final Map<Double, Double> priors;

        private final Map<Double, Map<Double, Integer>> valueCountPerClass;

        private final Map<Double, Integer> countPerClass;

        Builder(Attribute targetAttribute, double m, Map<Double, Double> priors) {
            this.targetAttribute = targetAttribute;
            this.m = m;
            this.priors = priors;
            this.valueCountPerClass = new HashMap<>();
            this.countPerClass = new HashMap<>();
        }

        @Override
        public void add(Example example) {
            final Map<Double, Integer> classCounts =
                    valueCountPerClass.computeIfAbsent(example.getValue(targetAttribute), value -> new HashMap<>());

            classCounts.merge(example.getLabel(), 1, Integer::sum);

            countPerClass.merge(example.getLabel(), 1, Integer::sum);
        }

        @Override
        public ProbabilityCalculator build() {
            return new NominalProbabilityCalculator(this);
        }
    }
}
