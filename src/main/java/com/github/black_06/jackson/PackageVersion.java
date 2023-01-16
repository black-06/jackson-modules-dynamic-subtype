package com.github.black_06.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.core.util.VersionUtil;

public class PackageVersion implements Versioned {

    public final static Version VERSION = VersionUtil.parseVersion(
            "2.14.0", "com.github.black-06.jackson", "jackson-modules-dynamic-subtype"
    );

    @Override
    public Version version() {
        return VERSION;
    }
}
