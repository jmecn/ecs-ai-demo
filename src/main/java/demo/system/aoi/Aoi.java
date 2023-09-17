package demo.system.aoi;

import com.simsilica.es.EntityComponent;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class Aoi implements EntityComponent {
    private final String group;

    private Long areaId;
    private int x;
    private int y;
    private int z;

    private final float radius;
    private final float radiusSquared;

    public Aoi(String group, float radius) {
        this.group = group;
        this.radius = radius;
        this.radiusSquared = radius * radius;
    }

    public void setArea(Long areaId, int x, int y, int z) {
        this.areaId = areaId;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}