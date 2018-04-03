package battila.rapidminer.extension.operator.mestimate;

import com.rapidminer.example.Example;

interface ProbabilityCalculator {
    double calculateFor(double attributeValue, double clazz);

    interface Builder {
        void add(Example example);

        ProbabilityCalculator build();
    }
}
