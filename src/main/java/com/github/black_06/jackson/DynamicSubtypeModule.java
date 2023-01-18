package com.github.black_06.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.google.auto.service.AutoService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Dynamic subtype module.
 * <p>
 * The module caches the subclass, so it's non-real-time.
 * It's for registering subtypes without annotating the parent class.
 * See <a href="https://github.com/FasterXML/jackson-databind/issues/2104">this issues<a/> in jackson-databind.
 * <p>
 * When not found in the cache, it loads and caches subclasses using SPI.
 * Therefore, we can {@link #unregisterType} a class and then module will reload this class's subclasses.
 */
@AutoService(Module.class)
public class DynamicSubtypeModule extends Module {

    /**
     * if true, module uses {@link java.util.ServiceLoader}.
     * if false, module uses {@link ServiceLoader} modified from Oracle JDK 1.8.0_291-b10
     * <p>
     * It's tested in Oracle JDK 1.8.0_291-b10 and Open JDK 17+35-LTS-2724
     * <p>
     * When {@link ServiceLoader} doesn't work in other version, open this setting.
     * NOTE: {@link java.util.ServiceLoader} must need a no args constructor in subclass.
     */
    private boolean useStandardServiceLoader;
    private final ConcurrentHashMap<Class<?>, List<NamedType>> subtypes = new ConcurrentHashMap<>();

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

    public void setUseStandardServiceLoader(boolean useStandardServiceLoader) {
        this.useStandardServiceLoader = useStandardServiceLoader;
    }

    /**
     * load parent's subclass by SPI.
     *
     * @param parent parent class.
     */
    @SuppressWarnings("unchecked")
    public <S> void registerTypes(Class<S> parent) {
        if (subtypes.containsKey(parent)) {
            return;
        }
        if (useStandardServiceLoader) {
            List<Class<S>> subclasses = new ArrayList<>();
            for (S instance : java.util.ServiceLoader.load(parent)) {
                subclasses.add((Class<S>) instance.getClass());
            }
            this.registerTypes(parent, subclasses);
        } else {
            this.registerTypes(parent, ServiceLoader.load(parent));
        }
    }

    /**
     * register subtypes without SPI.
     * Of course, you need to provide them :)
     */
    public <S> void registerTypes(Class<S> parent, Iterable<Class<S>> subclasses) {
        List<NamedType> result = new ArrayList<>();
        for (Class<S> subclass : subclasses) {
            result.addAll(findSubtypes(subclass, subclass::getAnnotation));
        }
        subtypes.put(parent, result);
    }

    public void unregisterType(Class<?> parent) {
        subtypes.remove(parent);
    }

    private static <S> List<NamedType> findSubtypes(Class<S> clazz, Function<Class<JsonSubType>, JsonSubType> getter) {
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
