package io.github.black.jackson;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import lombok.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * test work with {@link JsonSubTypes}
 */
public class WithJsonSubTypesTest {

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    public static Stream<Arguments> factory() {
        return Stream.of(
                Arguments.of(FirstChild.class, new FirstChild("hello")),
                Arguments.of(SecondChild.class, new SecondChild("world")),
                Arguments.of(FirstAppendChild.class, new FirstAppendChild(42)),
                Arguments.of(SecondAppendChild.class, new SecondAppendChild("42", Arrays.asList("hello", "foo", "bar"))),
                Arguments.of(ThirdAppendChild.class, new ThirdAppendChild("42", Arrays.asList("hello", "foo", "bar"), 3.1415926))
        );
    }

    @ParameterizedTest
    @MethodSource("factory")
    public <T extends Parent> void test(Class<T> clazz, T expected) {
        final Parent parent = expected;
        String json = assertDoesNotThrow(() -> mapper.writeValueAsString(parent));

        Parent unmarshal = assertDoesNotThrow(() -> mapper.readValue(json, Parent.class));
        T actual = assertInstanceOf(clazz, unmarshal);
        assertEquals(expected, actual);
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes(value = {
            @JsonSubTypes.Type(value = FirstChild.class, name = "first-child"),
            @JsonSubTypes.Type(value = SecondChild.class, name = "second-child"),
    })
    public interface Parent {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FirstChild implements Parent {
        private String foo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecondChild implements Parent {
        private String bar;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonSubType("first-append-child")
    @AutoService(Parent.class) // module requires spi
    public static class FirstAppendChild implements Parent {
        private Integer integer;
    }


    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @JsonSubType("second-append-child")
    @AutoService(Parent.class) // module requires spi
    public static class SecondAppendChild extends SecondChild {
        private List<String> list;

        public SecondAppendChild(String bar, List<String> list) {
            super(bar);
            this.list = list;
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @JsonSubType("third-append-child")
    @AutoService(Parent.class) // module requires spi
    public static class ThirdAppendChild extends SecondAppendChild {
        private double value;

        public ThirdAppendChild(String bar, List<String> list, double value) {
            super(bar, list);
            this.value = value;
        }
    }
}