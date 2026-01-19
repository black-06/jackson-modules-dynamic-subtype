# Update 2026-01-19

I have merged this module into [jackson-modules-base](https://github.com/FasterXML/jackson-modules-base), which has been
included since version 2.21.0. This repo are no longer maintained. Please use

```xml
<dependency>
    <groupId>com.fasterxml.jackson.module</groupId>
    <artifactId>jackson-module-spi-subtypes</artifactId>
    <version>2.21.0</version>
</dependency>
```

# Overview

Registering subtypes without annotating the parent class,
see [this](https://github.com/FasterXML/jackson-databind/issues/2104).

Implementation on SPI.

# Usage

Maven dependencies

```xml

<dependency>
    <groupId>io.github.black-06.jackson</groupId>
    <artifactId>jackson-modules-dynamic-subtype</artifactId>
    <version>2.14.0</version>
</dependency>
```

Registering modules.

```
ObjectMapper mapper = new ObjectMapper().registerModule(new DynamicSubtypeModule());
```

Alternatively, you can also auto-discover these modules with:

```
ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
```

Ensure that the parent class has at least the `JsonTypeInfo` annotation.

```java

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface Parent {
}
```

Add the `JsonSubType` annotation to your subclass.

```java
import io.github.black.jackson.JsonSubType;

@JsonSubType("first-child")
public class FirstChild {
    // ...
}
```

SPI: Put the subclasses in the `META-INF/services` directory under the interface.
Example: `META-INF/services/package.Parent`

```
package.FirstChild
```

Alternatively, you can also use the `auto-service` to auto-generate these files:

```java
import io.github.black.jackson.JsonSubType;
import com.google.auto.service.AutoService;

@AutoService(Parent.class)
@JsonSubType("first-child")
public class FirstChild {
    // ...
}
```

Done, enjoy it.

# More

I rewrote the ServiceLoader, it only returns class instead of instance.
It comes from **Oracle JDK 8**.
I mean, this module may **NOT WORK** in other jdk version.

Try this if it doesn't work: `new DynamicSubtypeModule().setUseStandardServiceLoader(true);`