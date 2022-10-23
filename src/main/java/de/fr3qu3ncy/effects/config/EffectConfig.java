package de.fr3qu3ncy.effects.config;

import de.fr3qu3ncy.easyconfig.core.annotations.ConfigurableField;
import de.fr3qu3ncy.easyconfig.core.serialization.Configurable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@ConfigurableField
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EffectConfig implements Configurable<EffectConfig> {

    private String displayName;
    private boolean persistLogin;
    private boolean persistDeath;

}
