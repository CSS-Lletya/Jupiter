package com.jupiter.game.player.attributes;

import java.util.Map;

/**
 * A wrapper class for a {@link Map}s value.
 * 
 * This class can provide additional functions for a maps key values and to help force type safety.
 *
 * @author Seven
 */
public final class AttributeValue<T extends Object> {

      /**
       * The actual value of this {@link AttributeValue}.
       */
      private final T value;

      /**
       * Creates a new {@link AttributeValue}.
       * 
       * @Param value
       *    The value to add.
       */
      public AttributeValue(T value) {
            this.value = value;
      }

      /**
       * Gets the actual value.
       * 
       * @Return The actual value.
       */
      public T getValue() {
            return value;
      }
      
      /**
       * Gets the type of this value.
       * 
       * @Return The type.
       */
      public Class<?> getType() {
            return value.getClass();
      }
      
      @Override
      public String toString() {
           return value.toString(); 
      }

}