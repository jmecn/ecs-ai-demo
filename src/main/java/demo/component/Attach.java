package demo.component;

import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityId;
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
public class Attach implements EntityComponent {
    private final EntityId target;

    public Attach(EntityId target) {
        this.target = target;
    }
}
