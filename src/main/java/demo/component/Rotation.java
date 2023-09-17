package demo.component;

import com.jme3.math.Quaternion;
import com.simsilica.es.*;
import lombok.Getter;
import lombok.ToString;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2023/9/16
 */
@Getter
@ToString
public class Rotation implements EntityComponent {

    private final Quaternion rotation;

    public Rotation(float facing) {
        rotation = new Quaternion().fromAngles(0, (float)facing, 0);
    }

    public Rotation(Quaternion rotation) {
        this.rotation = rotation;
    }
}
