package com.example.questionbank.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Writes down the dependency rules of the layered architecture, so a wrong import breaks the build.
 */
@AnalyzeClasses(packages = "com.example.questionbank", importOptions = ImportOption.DoNotIncludeTests.class)
class LayeringTest {

    private static final String DOMAIN = "com.example.questionbank.domain..";
    private static final String APPLICATION = "com.example.questionbank.application..";
    private static final String API = "com.example.questionbank.api..";
    private static final String INFRASTRUCTURE = "com.example.questionbank.infrastructure..";

    @ArchTest
    static final ArchRule domainDoesNotKnowJpaOrSpring = noClasses().that().resideInAPackage(DOMAIN)
            .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "org.springframework..",
                    "org.hibernate..")
            .because("the domain model must stay free of persistence annotations");

    @ArchTest
    static final ArchRule domainDoesNotKnowTheOtherLayers = noClasses().that().resideInAPackage(DOMAIN)
            .should().dependOnClassesThat().resideInAnyPackage(APPLICATION, API, INFRASTRUCTURE);

    @ArchTest
    static final ArchRule servicesDependOnContractsNotOnJpa = noClasses().that().resideInAPackage(APPLICATION)
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework.data..", "jakarta.persistence..",
                    INFRASTRUCTURE)
            .because("application services use repository contracts, never JpaRepository or records");

    @ArchTest
    static final ArchRule controllersDoNotTouchInfrastructure = noClasses().that().resideInAPackage(API)
            .should().dependOnClassesThat().resideInAnyPackage(INFRASTRUCTURE, "jakarta.persistence..");

    @ArchTest
    static final ArchRule infrastructureDoesNotUseTheUpperLayers = noClasses().that().resideInAPackage(INFRASTRUCTURE)
            .should().dependOnClassesThat().resideInAnyPackage(APPLICATION, API);

    @ArchTest
    static final ArchRule entitiesLiveOnlyInTheRecordPackage = classes().that()
            .areAnnotatedWith("jakarta.persistence.Entity").should()
            .resideInAPackage("com.example.questionbank.infrastructure.jpa.record..");

    @ArchTest
    static final ArchRule repositoryContractsAreInterfacesInTheDomain = classes().that()
            .haveSimpleNameEndingWith("Repository").and().resideInAPackage("..domain.repository..")
            .should().beInterfaces();

    @ArchTest
    static final ArchRule jpaRepositoriesImplementDomainContracts = classes().that()
            .haveSimpleNameEndingWith("RepositoryJpa").should().resideInAPackage(
                    "com.example.questionbank.infrastructure.jpa")
            .andShould().implement(JavaClass.Predicates.resideInAPackage(
                    "com.example.questionbank.domain.repository"));

    @ArchTest
    static final ArchRule noPortAndAdapterPackages = noClasses().should().resideInAnyPackage("..port..", "..adapter..",
            "..ports..", "..adapters..").because("the workshop asks for plain layers");
}
