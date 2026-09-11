package ncasa;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "ncasa", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {
    @ArchTest
    static final ArchRule domain_is_framework_free = noClasses()
            .that().resideInAnyPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..", "jakarta.servlet..");

    @ArchTest
    static final ArchRule application_does_not_depend_on_infrastructure = noClasses()
            .that().resideInAnyPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..");

    @ArchTest
    static final ArchRule expense_domain_is_isolated_from_other_bounded_contexts = noClasses()
            .that().resideInAnyPackage("..expense.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..household..", "..identityaccess..", "..common..");

    @ArchTest
    static final ArchRule expense_does_not_reach_household_infrastructure = noClasses()
            .that().resideInAnyPackage("..expense..")
            .should().dependOnClassesThat().resideInAnyPackage("..household.infrastructure..");

    @ArchTest
    static final ArchRule notification_domain_is_isolated = noClasses()
            .that().resideInAnyPackage("..notification.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..expense..", "..household..", "..identityaccess..", "..common..",
                    "org.springframework..", "jakarta.persistence..");

    @ArchTest
    static final ArchRule calendar_domain_is_isolated = noClasses()
            .that().resideInAnyPackage("..calendar.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..expense..", "..household..", "..identityaccess..", "..notification..", "..common..",
                    "org.springframework..", "jakarta.persistence..");

    @ArchTest
    static final ArchRule calendar_does_not_reach_other_infrastructure = noClasses()
            .that().resideInAnyPackage("..calendar..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..household.infrastructure..", "..expense.infrastructure..", "..notification.infrastructure..");
}
