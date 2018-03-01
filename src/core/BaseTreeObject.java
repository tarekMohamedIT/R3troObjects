package core;

/**
 * Created by tarek on 6/28/17.
 */
public abstract class BaseTreeObject {


    public abstract int getNodesCount();


    public abstract void removeNode(int position);

    public abstract void clearNodes();

    protected String makeSpaces(int count) {
        StringBuilder spaceString = new StringBuilder();
        for (int i = 0; i < count; i++) {
            spaceString.append("   ");
        }
        return spaceString.toString();
    }

    public void showTreeHierarchy() {
        showTreeHierarchy(this, this, 0);
    }

    protected abstract void showTreeHierarchy(BaseTreeObject startNode
            , BaseTreeObject currentNode
            , int startSpacing);

    protected abstract void showTreeHierarchy(BaseTreeObject startNode
            , BaseTreeObject currentNode
            , int startSpacing
            , OnTreeNodeShowingListener onTreeNodeShowingListener);

    public interface OnTreeNodeShowingListener {
        void showTreeNode(BaseTreeObject object);
    }
}
