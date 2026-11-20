package com.wesolve.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.wesolve")
class ModuleBoundaryTest {

  private static final DescribedPredicate<JavaClass> PLATFORM_INTERNALS =
      JavaClass.Predicates.resideInAPackage("com.wesolve.platform..")
          .and(
              DescribedPredicate.not(
                  JavaClass.Predicates.resideInAPackage("com.wesolve.platform.api..")));

  private static final DescribedPredicate<JavaClass> ACCESS_INTERNALS =
      JavaClass.Predicates.resideInAPackage("com.wesolve.access..")
          .and(
              DescribedPredicate.not(
                  JavaClass.Predicates.resideInAPackage("com.wesolve.access.api..")));

  @ArchTest
  static final ArchRule platformIsIndependent =
      noClasses()
          .that()
          .resideInAPackage("com.wesolve.platform..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "com.wesolve.access..", "com.wesolve.tickets..", "com.wesolve.assistant..");

  @ArchTest
  static final ArchRule accessDoesNotDependUpward =
      noClasses()
          .that()
          .resideInAPackage("com.wesolve.access..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("com.wesolve.tickets..", "com.wesolve.assistant..");

  @ArchTest
  static final ArchRule ticketsDoNotDependOnAssistant =
      noClasses()
          .that()
          .resideInAPackage("com.wesolve.tickets..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("com.wesolve.assistant..");

  @ArchTest
  static final ArchRule assistantDoesNotSkipDownward =
      noClasses()
          .that()
          .resideInAPackage("com.wesolve.assistant..")
          .should()
          .dependOnClassesThat(ACCESS_INTERNALS);

  @ArchTest
  static final ArchRule accessUsesPlatformApi =
      noClasses()
          .that()
          .resideInAPackage("com.wesolve.access..")
          .should()
          .dependOnClassesThat(PLATFORM_INTERNALS);

  @ArchTest
  static final ArchRule ticketsUseAccessApi =
      noClasses()
          .that()
          .resideInAPackage("com.wesolve.tickets..")
          .should()
          .dependOnClassesThat(ACCESS_INTERNALS);

  @ArchTest
  static final ArchRule ticketsUsePlatformApi =
      noClasses()
          .that()
          .resideInAPackage("com.wesolve.tickets..")
          .should()
          .dependOnClassesThat(PLATFORM_INTERNALS);
}
