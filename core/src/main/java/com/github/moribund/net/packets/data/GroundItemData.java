package com.github.moribund.net.packets.data;

import lombok.Getter;

import java.io.Serializable;

public class GroundItemData implements Serializable {
    private static final long serialVersionUID = 5508611406619988854L;

    @Getter
    private int itemId;
    @Getter
    private float x;
    @Getter
    private float y;
}