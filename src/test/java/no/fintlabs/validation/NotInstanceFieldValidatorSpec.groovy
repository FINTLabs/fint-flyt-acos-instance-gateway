package no.fintlabs.validation


import spock.lang.Specification

import javax.validation.ConstraintValidatorContext

class NotInstanceFieldValidatorSpec extends Specification {

    private NotInstanceFieldValidator validator = new NotInstanceFieldValidator()
    private ConstraintValidatorContext context = Mock(ConstraintValidatorContext)


    def "should validate null string as true"() {
        when:
        def result = validator.isValid(null, context)

        then:
        result
    }

    def "should validate empty string as true"() {
        when:
        def result = validator.isValid("", context)

        then:
        result
    }

    def "should validate string without \$if{ as true"() {
        when:
        def result = validator.isValid("some string without text we dont want", context)

        then:
        result
    }

    def "should validate string with \$if{ as false"() {
        when:
        def result = validator.isValid("some string with \$if{", context)

        then:
        !result
    }
}
