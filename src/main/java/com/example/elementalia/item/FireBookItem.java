package com.example.elementalia.item;

import com.example.elementalia.element.Element;

/**
 * The Fire Book — first of the elemental tomes. Casts a beam that erupts
 * lava and fire at the impact point.
 *
 * All behaviour is inherited from {@link ElementalTomeItem}; the element
 * is supplied via {@link #element()}.
 */
public class FireBookItem extends ElementalTomeItem {

    public FireBookItem(Properties properties) {
        super(properties);
    }

    @Override
    protected Element element() {
        return Element.FIRE;
    }
}
