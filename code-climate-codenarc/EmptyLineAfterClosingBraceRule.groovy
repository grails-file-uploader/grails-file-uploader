package com.causecode.quality.configurations.codenarc

import org.codenarc.rule.AbstractRule
import org.codenarc.source.SourceCode

/**
 * This is a custom rule that validates the presence of an empty line after the closing brace.
 * The case is ignored if there is another closing brace on the next line or it's a loop.
 *
 * @author Ankit Agrawal
 * @since 0.0.9
 */
class EmptyLineAfterClosingBraceRule extends AbstractRule {

    String name = 'EmptyLineAfterClosingBraceRule'
    String description = 'There should be an empty line after the closing (\"}\") brace.'
    int priority = 2

    @Override
    void applyTo(SourceCode sourceCode, List violations) {

        List<String> lines = sourceCode.lines

        for (int index = 1; index < lines.size(); index++) {
            String line = lines[index].trim()
            String previousLine = lines[index - 1].trim()

            if (previousLine.contains('}') && !(previousLine.contains('{')) && !(previousLine.contains('})')) &&
                    !line.contains('}') && !(line.isEmpty())) {
                violations.add(createViolation(index, previousLine,
                        'There should be an empty line after the closing brace.'))
            }
        }
    }
}
