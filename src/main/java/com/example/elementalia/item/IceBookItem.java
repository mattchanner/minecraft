package com.example.elementalia.item;

import com.example.elementalia.element.Element;

/** Ice tome — freezes victims and frosts the ground. */
public class IceBookItem extends ElementalTomeItem {

    public IceBookItem(Properties properties) {
        super(properties);
    }

    @Override
    protected Element element() {
        return Element.ICE;
    }
}
