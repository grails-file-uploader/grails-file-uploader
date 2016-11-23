/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package codenarc

import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codenarc.rule.AbstractAstVisitorRule
import org.codenarc.rule.AbstractMethodCallExpressionVisitor
import org.codenarc.util.AstUtil

/**
 * This is a custom codenarc rule that checks that all methods that returns a list from database must implement max
 * parameters.
 *
 * @author Nikhil Sharma
 * @since 1.1.0
 */
class GrailsMaxForListQueriesRule extends AbstractAstVisitorRule {
    String name = 'GrailsMaxForListQueries'
    String description = 'Any call to database that returns a list must have max value set.'
    int priority = 1
    Class astVisitorClass = GrailsMaxForListQueriesRuleAstVisitor
}

class GrailsMaxForListQueriesRuleAstVisitor extends AbstractMethodCallExpressionVisitor {

    @SuppressWarnings(['Instanceof'])
    @Override
    void visitMethodCallExpression(MethodCallExpression call) {
        String errorMessage = 'Add max parameter for this call'
        boolean isViolation

        def arguments = AstUtil.getMethodArguments(call)
        String methodName = call.methodAsString

        if (methodName == 'withCriteria') {
            arguments.each { argument ->
                if (argument instanceof ClosureExpression) {
                    if (!argument.code.text.contains('maxResults') && !argument.code.text.contains('rowCount')) {
                        isViolation = true
                    }
                }
            }
        }

        if (methodName == 'findAll' || methodName == 'list') {
            if (!arguments) {
                isViolation = true
            }

            arguments.each { argument ->
                if (argument instanceof MapExpression) {
                    def maxAsKey = argument.mapEntryExpressions.find { mapEntryExpression ->
                        mapEntryExpression.keyExpression.text == 'max'
                    }
                    isViolation = !maxAsKey
                }
            }
        }

        if (isViolation) {
            addViolation(call, errorMessage)
        }
    }
}
