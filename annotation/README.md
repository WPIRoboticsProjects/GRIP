# GRIP Annotation Processor

This subproject contains an annotation processor used to generate manifest files in the core project, used by the GRIP
runtime to discover operations, publishable data types, and aliases for XStream serialization for save files.

The annotation processor generates these files:

| Annotation | File |
|---|---|
| `@Description` | `/META-INF/operations` |
| `@PublishableObject` | `/META-INF/publishables` |
| `@XStreamAlias` | `/META-INF/xstream-aliases` |

Each file contains a list of the names of the classes annotated with the corresponding annotation, which is then read by
the `MetaInfReader` class in the GRIP core module.
