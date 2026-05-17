package com.example.elementalia.item;

import com.example.elementalia.element.Element;

/** Earth tome — knocks victims skyward with falling-block force. */
public class EarthBookItem extends ElementalTomeItem {

    public EarthBookItem(Properties properties) {
        super(properties);
    }

    @Override
    protected Element element() {
        return Element.EARTH;
    }
}
