package com.jupiter.game.player.attributes;

import java.util.EnumMap;

import com.jupiter.game.player.Player;

/**
 * A wrapper class for an {@link EnumMap} that provide type safety for its values.
 *
 * @author Seven
 * @author frostbit3
 */
public class AttributeMap<Props extends Enum<Props> & Attribute> {

    /**
     * The underlying map of attributes.
     *
     * EnumMap disallows null keys, but allows null values.
     */
    private final EnumMap<Props, AttributeValue<?>> attributes;

    public AttributeMap(Class<Props> enumType) {
        this.attributes = new EnumMap<>(enumType);
    }

    /**
     * Serves as a helper-function that places an {@link Props} into the map with a specified value type.
     *
     * @Param key
     *    The attribute to place.
     *
     * @Param value
     *    The type safe value.
     *
     * @ThroWs AttributeException
     *    The exception thrown the {@code key}s initial type is different than the specified {@code value}s initial type.
     */
    private <T extends Object> void put(Props key, AttributeValue<T> value) throws AttributeException {
        if (key.defaultValue().getClass() != value.getType()) {
            throw new AttributeException(key, value);
        }

        attributes.put(key, value);
    }

    /**
     * Places an {@link Props} into the map with a type-safe value.
     *
     * @Param key
     *    The attribute to place.
     *
     * @Param value
     *    The type safe value.
     */
    public <T extends Object> void put(Props key, T value) {
        try {
            put(key, new AttributeValue<T>(value));
        } catch (AttributeException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Places an {@link Props} into the map with a default value.
     *
     * @Param key
     *    The attribute to place.
     *
     */
    public void put(Props key) {
        try {
            put(key, new AttributeValue<>(key.defaultValue()));
        } catch (AttributeException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Places an {@link Props} into the map with a default value.
     *
     * @Param key
     *    The attribute to place.
     *
     */
    public void increment(Props key) {
        try {
            Integer value = value(key);
            put(key, value + 1);
        } catch (AttributeException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Toggles a {@link Boolean} type by switching false to true and true to false.
     *
     * @Param key
     *    The key who's value to toggle.
     */
    public void toggle(Props key) {
        Boolean value = (Boolean) attributes.get(key).getValue();
        try {
            toggle(key, new AttributeValue<Boolean>(value));
        } catch (AttributeException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * The helper function which validated the type and toggles the {@link Boolean} value if it is possible to do so.
     *
     * @Param key
     *    The key who's value to toggle.
     *
     * @Param key
     *    The key to toggle.
     */
    private void toggle(Props key, AttributeValue<Boolean> value) throws AttributeException {
        if (key.defaultValue().getClass() != value.getType()) {
            throw new AttributeException(key, value);
        }
        put(key, !value.getValue());
    }

    /**
     * Servers as a helper-function which determines if a {@code key} contains a specified {@code value}.
     *
     * @Param key
     *    The attribute to place.
     *
     * @Param value
     *    The type safe value.
     *
     * @Return {@code true} If this map contains the specified value. {@code false} otherwise.
     * @ThroWs AttributeException
     *    The exception thrown the {@code key}s initial type is different than the specified {@code value}s initial type.
     */
    private <T extends Object> boolean contains(Props key, AttributeValue<T> value) throws AttributeException {
        if (key.defaultValue().getClass() != value.getType()) {
            throw new AttributeException(key, value);
        }
        return attributes.get(key).getValue() == value.getValue();
    }

    /**
     * Determines if a {@code key} contains a specified {@code value}.
     *
     * @Param key
     *    The attribute to place.
     *
     * @Param value
     *    The type safe value.
     *
     * @Return {@code true} If this map contains the specified value. {@code false} otherwise.
     */
    public <T extends Object> boolean contains(Props key, T value) {
        try {
            return contains(key, new AttributeValue<T>(value));
        } catch (AttributeException ex) {
            ex.printStackTrace();
        }
        return attributes.get(key).getValue() == value;
    }

    /**
     * Gets the {@link AttributeValue} for a specified {@code key}.
     *
     * @Param key
     *    The key to get the attribute wrapper value for.
     *
     * @Return The wrapper for the value.
     */
    public AttributeValue<?> attribute(Props key) {
        return attributes.get(key);
    }

    /**
     * Gets the value from a specified {@code key}.
     *
     * This will grab the actual value, so you don't have to cast every time to attemp to get
     * the value from a key.
     *
     *  @Return The actual value.
     */
    @SuppressWarnings("unchecked")
	public <T extends Object> T value(Props key) {
        return (T) attributes.get(key).getValue();
    }

    /**
     * Gets the underlying map of attributes.
     *
     * @Return The underlying map {@link EnumMap}.
     */
    public EnumMap<Props, AttributeValue<?>> map() {
        return this.attributes;
    }

    /**
     * Gets a specified value as a {@link String} type.
     *
     * @Param key
     *    The key who's value to get as a string.
     *
     * @Return The value as a string.
     */
    public String valueAsString(Props key) {
        return attributes.get(key).toString();
    }

    /**
     * Determines if this map is empty.
     *
     * @Return {@code true} If this map is empty. {@code false} otherwise.
     */
    public boolean isEmpty() {
        return attributes.isEmpty();
    }

	public void stopAll(Player player) {
		stopAll(player, true);
	}

	public void stopAll(Player player, boolean stopWalk) {
		stopAll(player, stopWalk, true);
	}

	public void stopAll(Player player, boolean stopWalk, boolean stopInterface) {
		stopAll(player, stopWalk, stopInterface, true);
	}

	// as walk done clientsided - not anymore buddy
	public void stopAll(Player player, boolean stopWalk, boolean stopInterfaces, boolean stopActions) {
		if (stopInterfaces)
			player.getInterfaceManager().closeInterfaces();
		if (stopWalk){
			player.setCoordsEvent(null);
			player.setRouteEvent(null);
			player.resetWalkSteps();
			player.getPackets().sendResetMinimapFlag();
		}
		if (stopActions)
			player.getActionManager().forceStop();
		player.getCombatDefinitions().resetSpells(false);
	}
}