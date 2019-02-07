package com.geeselightning.zepr.pathfinding;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.utils.Array;
import com.geeselightning.zepr.Level;

public class GraphImp implements IndexedGraph<Node> {
    protected Array<Node> nodes = new Array<>();
    protected int capacity;

    public GraphImp() {
        super();
    }

    public GraphImp(int capacity) {
        this.capacity = capacity;
    }

    public GraphImp(Array<Node> nodes) {
        this.nodes = nodes;

        for (int x = 0; x < nodes.size; x++)
            nodes.get(x).index = x;
    }

    public Node getNodeByXY(int x, int y) {
        int modX = x / Level.tilePixelWidth;
        int modY = y / Level.tilePixelHeight;
        return nodes.get(Level.lvlTileWidth * modY + modX);
    }
    @Override
    public int getIndex(Node node) {
        return nodes.indexOf(node, true);
    }

    @Override
    public int getNodeCount() {
        return nodes.size;
    }

    @Override
    public Array<Connection<Node>> getConnections(Node fromNode) {
        return fromNode.getConnections();
    }
}
