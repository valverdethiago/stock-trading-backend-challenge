package com.altruist.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.*;

@Component
public class UuidConverter implements Converter<String, UUID> {

    @Override
    public UUID convert(@Nonnull String source) {
        return UUID.fromString(source);
    }
}
