package org.bhop;

import java.util.HashMap;

public class Styles {
    public static final double DefaultGains = 0.1;
    public static final HashMap<String, Integer> KeyOrder = new HashMap<>();
    public static class Style {
        private final String name;
        private final String shortname;
        private final String[] shortnames;
        private final int w;
        private final int a;
        private final int s;
        private final int d;
        private final int total;
        private double gains;
        public Style(
                //Style name and shortname
                String name,
                String[] shortnames,
                //0=Disabled 1=Enabled 2=Required
                int w,
                int a,
                int s,
                int d,
                //Minimum amount of keys required to be pressed (E.g. HSW=2)
                int total
        ) {
            this.name = name;
            this.shortname = shortnames[0];
            this.shortnames = shortnames;
            this.w = w;
            this.a = a;
            this.s = s;
            this.d = d;
            this.total = total;
            this.gains = DefaultGains;
        }
        public String GetName() {
            return this.name;
        }
        public String GetShortName() {
            return this.shortname;
        }
        public String[] GetShortNames() {
            return this.shortnames;
        }
        private Style SetGains(double value) {
            this.gains = value;
            return this;
        }
        public double GetGains() {
            return this.gains;
        }
        private boolean MissingRequiredKeys(int w, int a, int s, int d) {
            if (this.w == 2 && w != 1) return true;
            if (this.a == 2 && a != 1) return true;
            if (this.s == 2 && s != 1) return true;
            if (this.d == 2 && d != 1) return true;
            if (w + a + s + d < this.total) return true;
            return false;
        }
        public int ResolveDmA(int w, int a, int s, int d) {
            if (MissingRequiredKeys(w, a, s, d)) return 0;
            a = Math.min(this.a, a);
            d = Math.min(this.d, d);
            return d - a;
        }
        public int ResolveSmW(int w, int a, int s, int d) {
            if (MissingRequiredKeys(w, a, s, d)) return 0;
            s = Math.min(this.s, s);
            w = Math.min(this.w, w);
            return s - w;
        }
    }
    public static final Style[] AllStyles = new Style[]{
            new Style(
                    "Autohop",
                    new String[]{"auto", "a", "normal"},
                    1, 1, 1, 1, 1
            ),
            new Style(
                    "Half-Sideways",
                    new String[]{"halfsideways", "hsw"},
                    2, 1, 0, 1, 2
            ),
            new Style(
                    "Sideways",
                    new String[]{"sideways", "sw"},
                    1, 0, 1, 0, 1
            ),
    };
    public static final Style DefaultStyle = AllStyles[0];
}
