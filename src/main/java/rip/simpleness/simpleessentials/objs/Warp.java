package rip.simpleness.simpleessentials.objs;

import me.lucko.helper.serialize.Point;
import org.bukkit.entity.Player;

import java.util.Objects;

public final class Warp {

    private Point point;
    private String permission;

    public Warp(Point point, String permission) {
        this.point = point;
        this.permission = permission;
    }

    public Warp(Point point) {
        this(point, "");
    }

    public Point getPoint() {
        return point;
    }

    public String getPermission() {
        return permission;
    }

    public boolean hasPermission(Player player) {
        return permission.isEmpty() || player.hasPermission(permission);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Warp warp = (Warp) o;
        return Objects.equals(point, warp.point) &&
                Objects.equals(permission, warp.permission);
    }

    @Override
    public int hashCode() {
        return Objects.hash(point, permission);
    }

    @Override
    public String toString() {
        return "Warp{" +
                "point=" + point +
                ", permission='" + permission + '\'' +
                '}';
    }
}
