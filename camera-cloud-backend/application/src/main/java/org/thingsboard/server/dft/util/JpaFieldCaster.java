package org.thingsboard.server.dft.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class JpaFieldCaster {

    private JpaFieldCaster() {
    }

    @Nullable
    public static String getString(@Nullable Object object) {
        return Optional.ofNullable(object)
                .map(String::valueOf)
                .orElse(null);
    }

    @Nullable
    public static Long getLong(@Nullable Object object) {
        return Optional.ofNullable(getString(object))
                .map(Long::valueOf)
                .orElse(null);
    }

    @Nullable
    public static Boolean getBoolean(@Nullable Object object) {
        return Optional.ofNullable(getString(object))
                .map(Boolean::valueOf)
                .orElse(null);
    }


    @Nullable
    public static UUID getUUID(@Nullable Object object) {
        return Optional.ofNullable(getString(object))
                .map(UUID::fromString)
                .orElse(null);
    }


    @Nullable
    public static Integer getInt(@Nullable Object object) {
        return Optional.ofNullable(getString(object))
                .map(Integer::valueOf)
                .orElse(null);
    }

    @Nullable
    public static Date getDate(@Nullable Object object, String... pattern) {
        return Optional.ofNullable(getString(object))
                .map(str -> {
                    try {
                        return pattern.length == 0
                                ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.s").parse(str)
                                : new SimpleDateFormat(pattern[0]).parse(str);

                    } catch (ParseException e) {
                        log.error(e.getMessage());
                        return null;
                    }
                })
                .orElse(null);
    }
}
