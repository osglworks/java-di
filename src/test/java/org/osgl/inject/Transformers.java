package org.osgl.inject;

import org.osgl.util.S;

public class Transformers {
    public static class ToUpperCase extends ValueLoader.Base<String>  {
        @Override
        public String get() {
            return S.string(this.value()).toUpperCase();
        }
    }
    public static class ToLowerCase extends ValueLoader.Base<String> {
        @Override
        public String get() {
            return S.string(this.value()).toLowerCase();
        }
    }
}
