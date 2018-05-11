import org.codenarc.rule.AbstractRule
import org.codenarc.source.SourceCode

/**
 * This is a custom Codenarc rule that validates the file length.
 */
class FileLengthRule extends AbstractRule {

    String name = 'FileLengthRule'
    String description = 'Large files affect maintainability.'
    int priority = 3

    @Override
    void applyTo(SourceCode sourceCode, List violations) {

        int numberOfLines = sourceCode.lines.size()

        if (numberOfLines > 350) {
            violations.add(createViolation(numberOfLines - 1, null, "File $sourceCode.name contains $numberOfLines" +
                    ' lines of code which is greater than 350. Consider refactoring.'))
        }
    }
}
