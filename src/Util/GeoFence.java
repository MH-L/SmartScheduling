package Util;

import java.io.Serializable;
import java.util.List;

public class GeoFence implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 4000099632424184269L;

    private String id;

    private String description;

    private int layerID;

    private int layerOrder;

    private List<GeoFencePoint> vertices;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getLayerID() {
        return layerID;
    }

    public void setLayerID(int layerID) {
        this.layerID = layerID;
    }

    public int getLayerOrder() {
        return layerOrder;
    }

    public void setLayerOrder(int layerOrder) {
        this.layerOrder = layerOrder;
    }

    public List<GeoFencePoint> getVertices() {
        return vertices;
    }

    public void setVertices(List<GeoFencePoint> vertices) {
        this.vertices = vertices;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }
}
