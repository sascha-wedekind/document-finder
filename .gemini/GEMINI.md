# Project setup
1. Read `build.gradle` file find out which Java version the project uses. Also consider the libraries and only write code that match the libraries in their versions. 
2. The code in the project is structured with hexagonal architecture pattern. Ensure you also structure your code like that. Decide which existingmodule to use to suggest new modules if required.
3. As dependency injection framework google dagger is used.
4. The UI is build with JavaFX.


# Java Test Guidelines

When writing Java tests, please follow these guidelines:

1.  **Structure:** Use the 'Arrange, Act, Assert' pattern to structure your test methods, with comments to separate the sections.
2.  **Naming:** Name the subject under test instance 'sut'.
3.  **Test Method Naming:** Name test methods using the pattern `[METHOD_UNDER_TEST]_[EXPECTED_RESULT]_[PARAMETERS_USED]`. For example: `isValid_returnsTrue_whenGivenOTPIsKnownByTheService`.
4.  **Assertions:** Use AssertJ for all assertions.
