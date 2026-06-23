package net.nukebob.chameleon.texture;

public class Pixel {
    public int x;
    public int y;

    public Pixel (int x, int y) {
        this.x=x;
        this.y=y;
    }
    public Pixel (int x) {
        this.x=x;
        this.y=x;
    }

    public Pixel add (int x, int y) {
        this.x += x;
        this.y += y;

        return this;
    }

    public Pixel add (Pixel pixel) {
        this.x += pixel.x;
        this.y +=  pixel.y;

        return this;
    }
}
