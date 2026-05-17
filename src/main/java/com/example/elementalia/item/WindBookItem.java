package com.example.elementalia.item;

import com.example.elementalia.element.Element;

/** Wind tome — shockwave that blasts victims outward, no ground change. */
public class WindBookItem extends ElementalTomeItem {

    public WindBookItem(Properties properties) {
        super(properties);
    }

    @Override
    protected Element element() {
        return Element.WIND;
    }
}
