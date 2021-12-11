package com.jupiter.game.player.attributes;

/**
 * The {@link RuntimeException} implementation specifically for {@link AttributeType}s.
 * 
 * @author Seven
 */
public final class AttributeException extends RuntimeException {

      private static final long serialVersionUID = 1L;

      /**
       * Creates a new {@link AttributeException}.
       * 
       * @Param key
       *    The key or this attribute.
       *    
       * @Param value
       *    The value for this attribute.
       */
      public AttributeException(AttributeType key, AttributeValue<?> value) {
            super(String.format("Invalid value type: %s for [key=%s], only accepts type of %s", value.getType().getSimpleName(), key.name().toLowerCase(), key.defaultValue().getClass().getSimpleName()));
      }
      
}