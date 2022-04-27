package com.laysan.autojob.core.utils;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpCookie;
import java.util.Base64;

public class HttpCookieDeSerializer {
    // TODO: need to be changed?
    private final String fieldValueDelimiter = "===";
    // TODO: need to be changed?
    private final String fieldValuePairDelimiter = "###";

    public HttpCookieDeSerializer() {
        super();
    }

    public String decode(final String string) {
        return new String(Base64.getUrlDecoder().decode(string));
    }

    public HttpCookie decodeAndDeserialize(final String string) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        final String decoded = this.decode(string);
        // TODO: remove sysout
        System.out.println(decoded);
        return this.deserialize(decoded);
    }

    public HttpCookie deserialize(final String decoded) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        final String name = this.preGet(decoded, "name");
        final String value = this.preGet(decoded, "value");
        final HttpCookie cookie = new HttpCookie(name, value);
        final String[] fieldsAndValues = decoded.split("(" + this.fieldValuePairDelimiter + ")");
        for (final String fieldAndValue : fieldsAndValues) {
            final String[] fieldAndValueSplitted = fieldAndValue.split("(" + this.fieldValueDelimiter + ")");
            final Field field = HttpCookie.class.getDeclaredField(fieldAndValueSplitted[0]);
            if (Modifier.isFinal(field.getModifiers())) {
                // ???
                // continue;
            }
            field.setAccessible(true);
            final Class<?> type = field.getType();
            if (String.class.equals(type)) {
                field.set(cookie, this.convertNullStringToNullObject(fieldAndValueSplitted[1]));
            } else if (Long.TYPE.equals(type)) {
                field.setLong(cookie, Long.parseLong(fieldAndValueSplitted[1]));
            } else if (Integer.TYPE.equals(type)) {
                field.setInt(cookie, Integer.parseInt(fieldAndValueSplitted[1]));
            } else if (Boolean.TYPE.equals(type)) {
                field.setBoolean(cookie, Boolean.parseBoolean(fieldAndValueSplitted[1]));
            }
        }
        return cookie;
    }

    public String encode(final String string) {
        return Base64.getUrlEncoder().encodeToString(string.getBytes());
    }

    public String serialize(final HttpCookie cookie) throws IllegalAccessException, IllegalArgumentException {
        final StringBuilder builder = new StringBuilder();
        final Field[] fields = HttpCookie.class.getDeclaredFields();
        boolean first = true;
        for (final Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (!first) {
                builder.append(this.fieldValuePairDelimiter);
            }
            builder.append(field.getName());
            builder.append(this.fieldValueDelimiter);
            final Class<?> type = field.getType();
            field.setAccessible(true);
            if (String.class.equals(type)) {
                builder.append((String) field.get(cookie));
            } else if (Long.TYPE.equals(type)) {
                builder.append(Long.toString(field.getLong(cookie)));
            } else if (Integer.TYPE.equals(type)) {
                builder.append(Integer.toString(field.getInt(cookie)));
            } else if (Boolean.TYPE.equals(type)) {
                builder.append(Boolean.toString(field.getBoolean(cookie)));
            }
            first = false;
        }
        final String serialized = builder.toString();
        return serialized;
    }

    public String serializeAndEncode(final HttpCookie cookie) throws IllegalAccessException, IllegalArgumentException {
        final String serialized = this.serialize(cookie);
        // TODO: remove sysout
        System.out.println(serialized);
        return this.encode(serialized);
    }

    private Object convertNullStringToNullObject(final String string) {
        if ("null".equals(string)) {
            return null;
        }
        return string;
    }

    private String preGet(final String decoded, final String fieldName) {
        final String[] fieldsAndValues = decoded.split("(" + this.fieldValuePairDelimiter + ")");
        for (final String fieldAndValue : fieldsAndValues) {
            if (fieldAndValue.startsWith(fieldName + this.fieldValueDelimiter)) {
                return fieldAndValue.split("(" + this.fieldValueDelimiter + ")")[1];
            }
        }
        return null;
    }

    public static void main(final String[] args) {
        final HttpCookieDeSerializer hcds = new HttpCookieDeSerializer();
        try {
            final HttpCookie cookie = new HttpCookie("myCookie", "first");
            final String serializedAndEncoded = hcds.serializeAndEncode(cookie);
            // TODO: remove sysout
            System.out.println(serializedAndEncoded);
            final HttpCookie other = hcds.decodeAndDeserialize(serializedAndEncoded);
            // TODO: remove sysout
            System.out.println(cookie.equals(other));
            Cookie c = new Cookie.Builder().name("xxx").value("ooo").domain("baidu.com").build();
            System.out.println(c.toString());
            Cookie parse = Cookie.Companion.parse(HttpUrl.parse("http://www.baidu.com"), c.toString$okhttp(false));
            System.out.println(c.toString$okhttp(false));
            System.out.println(c.equals(parse));
        } catch (final Throwable t) {
            t.printStackTrace();
        }
    }
}