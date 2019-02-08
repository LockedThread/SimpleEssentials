package rip.simpleness.simpleessentials.modules;

import me.lucko.helper.Events;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import rip.simpleness.simpleessentials.SimpleEssentials;

import javax.annotation.Nonnull;

public class ModuleFixes implements TerminableModule {

    private static final SimpleEssentials INSTANCE = SimpleEssentials.getInstance();

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        Events.subscribe(WeatherChangeEvent.class)
                .filter(WeatherChangeEvent::toWeatherState)
                .filter(event -> INSTANCE.isDisableWeather())
                .handler(event -> event.setCancelled(true));

        Events.subscribe(ThunderChangeEvent.class)
                .filter(ThunderChangeEvent::toThunderState)
                .filter(event -> INSTANCE.isDisableWeather())
                .handler(event -> event.setCancelled(true));
    }
}
