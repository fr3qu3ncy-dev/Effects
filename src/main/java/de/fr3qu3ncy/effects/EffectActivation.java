package de.fr3qu3ncy.effects;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EffectActivation {

    HOLDING("Holding", "When Holding"),
    MAIN_HAND("Main Hand", "When In Main Hand"),
    OFF_HAND("Off Hand", "When In Off Hand"),
    HEAD("Head", "When On Head"),
    BODY("Body", "When On Body");

    private final String name;
    private final String description;

}
