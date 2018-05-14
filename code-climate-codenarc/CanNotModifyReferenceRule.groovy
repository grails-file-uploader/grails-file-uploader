package com.causecode.quality.configurations.codenarc

import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codenarc.rule.AbstractAstVisitor
import org.codenarc.rule.AbstractAstVisitorRule

import java.util.stream.Collectors

/**
 * Codenarc rule to avoid manipulating method/closure parameter.
 * @author Kshitij Mandloi
 * @since 1.0.1
 */
class CanNotModifyReferenceRule extends AbstractAstVisitorRule {

    String name = 'CannotModifyReference'
    int priority = 1
    String description = 'Parameters of method and closure cannot be modified. Use a temporary variable.'
    Class astVisitorClass = CanNotModifyReferenceAstVisitor

}

class CanNotModifyReferenceAstVisitor extends AbstractAstVisitor {

    private List<String> currentMethodParameterNames = []
    private List<String> currentClosureParameterNames = []

    @Override
    protected void visitMethodEx(MethodNode node) {
        currentMethodParameterNames = Arrays.stream(node.parameters)
                .map { Parameter parameter -> parameter.name }.collect(Collectors.toList())
        super.visitMethodEx(node)
    }

    @Override
    protected void visitMethodComplete(MethodNode node) {
        super.visitMethodComplete(node)
        currentMethodParameterNames = []
    }

    @Override
    void visitClosureExpression(ClosureExpression expression) {
        String parameterNames = expression.parameters?.text ?: []
        currentClosureParameterNames.addAll(parameterNames)
        super.visitClosureExpression(expression)
        currentClosureParameterNames.removeAll(parameterNames)
    }

    @Override
    void visitBinaryExpression(BinaryExpression expression) {
        if (isFirstVisit(expression) && isManipulatingAParameter(expression)) {
            String name = expression.leftExpression.text
            addViolation(expression, "The method parameter [$name] in class $currentClassName was manipulated. " +
                    "Use a temporary variable instead.")
        }
        super.visitBinaryExpression(expression)
    }

    /**
     * Utility method to check if a method is manipulating a parameter.
     * @param expression
     * @return true, if parameter manipulation is happening
     */
    private boolean isManipulatingAParameter(BinaryExpression expression) {
        expression.operation.text in ['=', '+=', '-=', '*=', '/=', '**'] &&
                expression.leftExpression instanceof PropertyExpression &&
                isCurrentParameterName(expression.leftExpression.text)
    }

    /**
     * Utility method to check if the leftExpression contains current method closure/parameter or not.
     * @param name
     * @return true, if leftExpression is current method/closure parameter
     */
    private boolean isCurrentParameterName(String name) {
        if (!name) {
            return false
        }

        List tokens = []
        tokens.addAll(currentMethodParameterNames)
        tokens.addAll(currentClosureParameterNames)

        return tokens.stream().anyMatch() { String param ->
            name.split('\\.')[0] == param
        }
    }
}

