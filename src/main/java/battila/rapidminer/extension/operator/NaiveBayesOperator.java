package battila.rapidminer.extension.operator;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.LogService;

public class NaiveBayesOperator extends Operator {
    public NaiveBayesOperator(OperatorDescription description) {
        super(description);
    }

    @Override
    public void doWork() throws OperatorException {
        LogService.getRoot().info("Working!");
    }
}
