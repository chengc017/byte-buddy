package net.bytebuddy.matcher;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.generic.GenericTypeDescription;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Matches a method description by its general characteristics which are represented as a
 * {@link net.bytebuddy.matcher.MethodSortMatcher.Sort}.
 *
 * @param <T> The type of the matched entity.
 */
public class MethodSortMatcher<T extends MethodDescription> extends ElementMatcher.Junction.AbstractBase<T> {

    /**
     * The sort of method description to be matched by this element matcher.
     */
    private final Sort sort;

    /**
     * Creates a new element matcher that matches a specific sort of method description.
     *
     * @param sort The sort of method description to be matched by this element matcher.
     */
    public MethodSortMatcher(Sort sort) {
        this.sort = sort;
    }

    @Override
    public boolean matches(T target) {
        return sort.isSort(target);
    }

    @Override
    public boolean equals(Object other) {
        return this == other || !(other == null || getClass() != other.getClass())
                && sort == ((MethodSortMatcher) other).sort;
    }

    @Override
    public int hashCode() {
        return sort.hashCode();
    }

    @Override
    public String toString() {
        return sort.getDescription();
    }

    /**
     * Represents a specific characteristic of a method description.
     */
    public enum Sort {

        /**
         * Matches method descriptions that represent methods, not constructors or the type initializer.
         */
        METHOD("isMethod()") {
            @Override
            protected boolean isSort(MethodDescription target) {
                return target.isMethod();
            }
        },


        /**
         * Matches method descriptions that represent constructors, not methods or the type initializer.
         */
        CONSTRUCTOR("isConstructor()") {
            @Override
            protected boolean isSort(MethodDescription target) {
                return target.isConstructor();
            }
        },

        /**
         * Matches method descriptions that represent the type initializers.
         */
        TYPE_INITIALIZER("isTypeInitializer()") {
            @Override
            protected boolean isSort(MethodDescription target) {
                return target.isTypeInitializer();
            }
        },

        /**
         * Matches method descriptions that are overridable.
         */
        OVERRIDABLE("isOverridable()") {
            @Override
            protected boolean isSort(MethodDescription target) {
                return target.isOverridable();
            }
        },

        /**
         * Matches method descriptions that represent bridge methods that are implemented in order to increase
         * a method's visibility in a subtype.
         */
        VISIBILITY_BRIDGE("isVisibilityBridge()") {
            @Override
            protected boolean isSort(MethodDescription target) {
                if (target.isBridge()) {
                    if (target.getDeclaringType().asRawType().isInterface()) {
                        return false;
                    }
                    for (GenericTypeDescription superType : target.getDeclaringType().asRawType().getSuperType()) {
                        for (MethodDescription methodDescription : superType.getDeclaredMethods()) {
                            if (methodDescription.asDefined().asToken().accept(GenericTypeDescription.Visitor.TypeErasing.INSTANCE).equals(target.asToken())) {
                                return !methodDescription.isBridge() && target.getDeclaringType().asRawType().getDeclaredMethods()
                                        .filter(not(isBridge())
                                                .and(hasMethodName(target.getInternalName()))
                                                .and(takesArguments(target.getParameters().asTypeList().asRawTypes())))
                                        .isEmpty();
                            }
                        }
                    }
                }
                return false;
            }
        },

        TYPE_VARIABLE_BRIDGE("isTypeVariableBridge()") {
            @Override
            protected boolean isSort(MethodDescription target) {
                return false;
            }
        },

        TYPE_BRIDGE("isTypeBridge()") {
            @Override
            protected boolean isSort(MethodDescription target) {
                return target.isBridge() && !VISIBILITY_BRIDGE.isSort(target);
            }
        },

        /**
         * Matches method descriptions that represent Java 8 default methods.
         */
        DEFAULT_METHOD("isDefaultMethod()") {
            @Override
            protected boolean isSort(MethodDescription target) {
                return target.isDefaultMethod();
            }
        };

        /**
         * A textual representation of the method sort that is represented by this instance.
         */
        private final String description;

        /**
         * Creates a new method sort representation.
         *
         * @param description A textual representation of the method sort that is represented by this instance.
         */
        Sort(String description) {
            this.description = description;
        }

        /**
         * Determines if a method description is of the represented method sort.
         *
         * @param target A textual representation of the method sort that is represented by this instance.
         * @return {@code true} if the given method if of the method sort that is represented by this instance.
         */
        protected abstract boolean isSort(MethodDescription target);

        /**
         * Returns a textual representation of this instance's method sort.
         *
         * @return A textual representation of this instance's method sort.
         */
        protected String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return "MethodSortMatcher.Sort." + name();
        }
    }
}