/*
 * Copyright (c) 2017-Present, Niteo Consulting Private Limited, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.quality.configurations.codenarc

import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codenarc.rule.AbstractAstVisitorRule
import org.codenarc.rule.AbstractMethodVisitor

/**
 * This is a custom rule that validates the method arguments. Conditions for violation -
 * If method has less than 3 arguments and any of them is a Map.
 *
 * @author Ankit Agrawal
 * @since 0.0.9
 */
class MapAsMethodParameterRule extends AbstractAstVisitorRule {
    String name = 'MapAsMethodParameterRule'
    String description = 'Verifies that the method parameters are valid. If the number of parameters are less' +
            ' than 3 and any of them is a Map then this error is thrown. It is advised to pass separate parameters ' +
            ' instead of passing the complete Map.'
    int priority = 3
    Class astVisitorClass = MapAsMethodParameterRuleAstVisitor
}

class MapAsMethodParameterRuleAstVisitor extends AbstractMethodVisitor {

    @Override
    void visitMethod(MethodNode node) {
        Parameter[] arguments = node.parameters
        int numberOfArgs = arguments.size()

        arguments.each { argument ->
            if (argument.type.toString() == 'Map' && numberOfArgs < 3) {
                addViolation(node, 'Passing the complete request parameter as arguments should be avoided.')
            }
        }
    }
}