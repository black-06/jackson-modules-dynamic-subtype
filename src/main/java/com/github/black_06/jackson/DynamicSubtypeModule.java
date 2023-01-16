package com.github.black_06.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.google.auto.service.AutoService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

@AutoService(Module.class)
public class DynamicSubtypeModule extends Module {
    private final HashMap<Class<?>, List<NamedType>> subtypes = new HashMap<>();

    @Override
    public String getModuleName() {
        return PackageVersion.VERSION.getArtifactId();
    }

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    @Override
    public void setupModule(SetupContext context) {
        context.insertAnnotationIntrospector(new AnnotationIntrospector() {
            @Override
            public Version version() {
                return PackageVersion.VERSION;
            }

            @Override
            public List<NamedType> findSubtypes(Annotated a) {
                registerTypes(a.getRawType());

                List<NamedType> list1 = DynamicSubtypeModule.findSubtypes(a.getRawType(), a::getAnnotation);
                List<NamedType> list2 = subtypes.getOrDefault(a.getRawType(), Collections.emptyList());

                if (list1.isEmpty()) return list2;
                if (list2.isEmpty()) return list1;
                List<NamedType> list = new ArrayList<>(list1.size() + list2.size());
                list.addAll(list1);
                list.addAll(list2);
                return list;
            }
        });
    }

    private void registerTypes(Class<?> parent) {
        if (subtypes.containsKey(parent)) {
            return;
        }
        List<NamedType> result = new ArrayList<>();
        for (Class<?> subclass : ServiceLoader.load(parent)) {
            result.addAll(findSubtypes(subclass, subclass::getAnnotation));
        }
        subtypes.put(parent, result);
    }

    private static List<NamedType> findSubtypes(Class<?> clazz, Function<Class<JsonSubType>, JsonSubType> getter) {
        if (clazz == null) {
            return Collections.emptyList();
        }
        JsonSubType subtype = getter.apply(JsonSubType.class);
        if (subtype == null) {
            return Collections.emptyList();
        }
        List<NamedType> result = new ArrayList<>();
        result.add(new NamedType(clazz, subtype.value()));
        // [databind#2761]: alternative set of names to use
        for (String name : subtype.names()) {
            result.add(new NamedType(clazz, name));
        }
        return result;
    }
}
