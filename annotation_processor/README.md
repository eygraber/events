# Events Annotation Processor

## Testing

* Open the Run Configurations window
  - From the configuration window on the left, select Add new configuration (+) and select Gradle
    - Name:                     Build annotation processor test classes
    - Gradle project:           events:annotation_processor
    - Tasks:                    testClasses
    - Apply your changes
  - From the configuration window on the left, select Add new configuration (+) and select JUnit
    - Test kind:                All in package
    - Package:                  <leave empty>
    - Search for tests:         In single module
    - VM options:               -ea -Xmx2048m -XX:+HeapDumpOnOutOfMemoryError
    - Working directory:        annotation_processor
    - Use classpath of module:  annotation_processor
    - Before launch
      - + -> Run another configuration -> Gradle - Build annotation processor test classes (the configuration created in the step above)
      - Make
    - Apply your changes