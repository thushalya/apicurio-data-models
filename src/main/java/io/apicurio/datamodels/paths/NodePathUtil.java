package io.apicurio.datamodels.paths;

import java.util.List;
import java.util.Map;

import io.apicurio.datamodels.TraverserDirection;
import io.apicurio.datamodels.VisitorUtil;
import io.apicurio.datamodels.models.MappedNode;
import io.apicurio.datamodels.models.Node;
import io.apicurio.datamodels.models.union.Union;
import io.apicurio.datamodels.util.NodeUtil;

public class NodePathUtil {

    public static NodePath createNodePath(Node node) {
        NodePathVisitor visitor = new NodePathVisitor(node);
        VisitorUtil.visitTree(node.root(), visitor, TraverserDirection.down);
        return visitor.getNodePath();
    }

    public static NodePath parseNodePath(String path) {
        return NodePath.parse(path);
    }

    @SuppressWarnings("rawtypes")
    public static Node resolveNodePath(NodePath nodePath, Node from) {
        List<NodePathSegment> segments = nodePath.getSegments();
        Object current = from;
        for (NodePathSegment segment : segments) {
            if (!segment.isIndex()) {
                current = NodeUtil.getProperty(current, segment.getValue());
            } else {
                if (NodeUtil.isUnion(current)) {
                    Union union = (Union) current;
                    current = union.unionValue();
                }

                if (NodeUtil.isNode(current)) {
                    MappedNode mappedNode = (MappedNode) current;
                    current = mappedNode.getItem(segment.getValue());
                } else if (NodeUtil.isList(current)) {
                    int index = NodeUtil.toInteger(segment.getValue());
                    List list = (List) current;
                    current = list.get(index);
                } else if (NodeUtil.isMap(current)) {
                    Map map = (Map) current;
                    current = NodeUtil.getMapItem(map, segment.getValue());
                }
            }
        }
        if (NodeUtil.isNode(current)) {
            return (Node) current;
        } else {
            return null;
        }
    }

}
