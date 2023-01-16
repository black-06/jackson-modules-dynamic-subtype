package com.github.black_06.jackson;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * test work without {@link JsonSubTypes}
 */
public class WithoutJsonSubTypesTest {
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    public interface Parent {
    }

    @Data
    @JsonSubType("first-child")
    @AutoService(Parent.class) // module requires spi
    public static class FirstChild implements Parent {
        private String foo;
    }

    @Test
    public void testFirstChild() {
        FirstChild child = new FirstChild();
        child.setFoo("hello");
        final Parent parent = child;
        String json = assertDoesNotThrow(() -> mapper.writeValueAsString(parent));

        // {"type":"first-child","foo":"hello"}

        Parent unmarshal = assertDoesNotThrow(() -> mapper.readValue(json, Parent.class));
        FirstChild actual = assertInstanceOf(FirstChild.class, unmarshal);
        assertEquals("hello", actual.getFoo());
    }

    @Data
    @JsonSubType("second-child")
    @AutoService(Parent.class) // module requires spi
    public static class SecondChild implements Parent {
        private String bar;
    }

    @Test
    public void testSecondChild() {
        SecondChild child = new SecondChild();
        child.setBar("world");
        final Parent parent = child;
        String json = assertDoesNotThrow(() -> mapper.writeValueAsString(parent));

        // {"type":"second-child","bar":"world"}

        Parent unmarshal = assertDoesNotThrow(() -> mapper.readValue(json, Parent.class));
        SecondChild actual = assertInstanceOf(SecondChild.class, unmarshal);
        assertEquals("world", actual.getBar());
    }
}