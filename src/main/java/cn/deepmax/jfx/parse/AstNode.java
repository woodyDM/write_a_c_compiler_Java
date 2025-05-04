package cn.deepmax.jfx.parse;

import java.util.List;

public interface AstNode {

    List<AstNode> getChildren();

    String prettyString();
}
