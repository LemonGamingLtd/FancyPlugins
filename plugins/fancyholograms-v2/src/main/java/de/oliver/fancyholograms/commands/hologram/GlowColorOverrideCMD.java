package de.oliver.fancyholograms.commands.hologram;

import com.google.common.primitives.Ints;
import de.oliver.fancyholograms.FancyHolograms;
import de.oliver.fancyholograms.api.data.DisplayHologramData;
import de.oliver.fancyholograms.api.events.HologramUpdateEvent;
import de.oliver.fancyholograms.api.hologram.Hologram;
import de.oliver.fancyholograms.commands.HologramCMD;
import de.oliver.fancyholograms.commands.Subcommand;
import de.oliver.fancylib.MessageHelper;
import org.bukkit.Color;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GlowColorOverrideCMD implements Subcommand {

    @Override
    public List<String> tabcompletion(@NotNull CommandSender player, @Nullable Hologram hologram, @NotNull String[] args) {
        return null;
    }

    @Override
    public boolean run(@NotNull CommandSender player, @Nullable Hologram hologram, @NotNull String[] args) {
        if (!(player.hasPermission("fancyholograms.hologram.edit.glow_color_override"))) {
            MessageHelper.error(player, "You don't have the required permission to change the glow color override of a hologram");
            return false;
        }

        final var red = Ints.tryParse(args[3]);
        final var green = args.length >= 6 ? Ints.tryParse(args[4]) : red;
        final var blue = args.length >= 6 ? Ints.tryParse(args[5]) : red;

        if (red == null || green == null || blue == null) {
            MessageHelper.error(player, "Could not parse RGB colors");
            return false;
        }

        if (red > 255 || red < 0 || green > 255 || green < 0 || blue > 255 || blue < 0) {
            MessageHelper.error(player, "RGB color values must be between 0 and 255");
            return false;
        }

        if (!(hologram.getData() instanceof DisplayHologramData displayData)) {
            MessageHelper.error(player, "This command can only be used on display holograms");
            return false;
        }

        if (displayData.getGlowColorOverride() != null &&
            Ints.compare(red, displayData.getGlowColorOverride().getRed()) == 0 &&
            Ints.compare(green, displayData.getGlowColorOverride().getGreen()) == 0 &&
            Ints.compare(blue, displayData.getGlowColorOverride().getBlue()) == 0) {
            MessageHelper.warning(player, "This hologram already has that glow color override");
            return false;
        }

        final var copied = displayData.copy(displayData.getName());
        copied.setGlowColorOverride(Color.fromRGB(red, green, blue));

        if (!HologramCMD.callModificationEvent(hologram, player, copied, HologramUpdateEvent.HologramModification.GLOW_COLOR_OVERRIDE)) {
            return false;
        }

        if (displayData.getGlowColorOverride() != null &&
            copied.getGlowColorOverride() != null &&
            Ints.compare(copied.getGlowColorOverride().getRed(), displayData.getGlowColorOverride().getRed()) == 0 &&
            Ints.compare(copied.getGlowColorOverride().getGreen(), displayData.getGlowColorOverride().getGreen()) == 0 &&
            Ints.compare(copied.getGlowColorOverride().getBlue(), displayData.getGlowColorOverride().getBlue()) == 0) {
            MessageHelper.warning(player, "This hologram is already at this scale");
            return false;
        }

        displayData.setGlowColorOverride(copied.getGlowColorOverride());

        if (FancyHolograms.get().getHologramConfiguration().isSaveOnChangedEnabled()) {
            FancyHolograms.get().getHologramStorage().save(hologram);
        }

        MessageHelper.success(player, "Changed glow color override to RGB=" + red + ", " + green + ", " + blue);
        return true;
    }
}
