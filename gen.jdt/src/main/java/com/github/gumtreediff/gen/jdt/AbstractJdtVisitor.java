/*
 * This file is part of GumTree.
 *
 * GumTree is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GumTree is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GumTree.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2011-2015 Jean-Rémy Falleri <jr.falleri@gmail.com>
 * Copyright 2011-2015 Floréal Morandat <florealm@gmail.com>
 */

package com.github.gumtreediff.gen.jdt;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import com.github.gumtreediff.gen.jdt.cd.EntityType;
import com.github.gumtreediff.tree.*;
import com.github.gumtreediff.tree.Type;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.*;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

import static com.github.gumtreediff.tree.TypeSet.type;

public abstract class AbstractJdtVisitor extends ASTVisitor {

    protected TreeContext context = new TreeContext();

    protected Deque<ITree> trees = new ArrayDeque<>();

    public AbstractJdtVisitor() {
        super(true);
    }

    public TreeContext getTreeContext() {
        return context;
    }

    protected void pushNode(ASTNode n, String label) {
        push(n, nodeAsSymbol(n), label, n.getStartPosition(), n.getLength());
    }

    protected void pushFakeNode(EntityType n, int startPosition, int length) {
        Type type = type(n.name()); // FIXME is that consistent with AbstractJDTVisitor.type
        push(null, type,"", startPosition, length);
    }

    protected void push(ASTNode n, Type type, String label, int startPosition, int length) {
        ITree t = context.createTree(type, label);
        t.setPos(startPosition);
        t.setLength(length);

        if (trees.isEmpty())
            context.setRoot(t);
        else {
            ITree parent = trees.peek();
            t.setParentAndUpdateChildren(parent);
        }

        if (n instanceof TypeDeclaration)
            t.setMetadata("id", getId((TypeDeclaration) n));
        else if (n instanceof MethodDeclaration)
            t.setMetadata("id", getId((MethodDeclaration) n));
        else if (n instanceof FieldDeclaration)
            t.setMetadata("id", getId((FieldDeclaration) n));
        else if (n instanceof EnumDeclaration)
            t.setMetadata("id", getId((EnumDeclaration) n));

        trees.push(t);
    }

    private String getId(TypeDeclaration d) {
        return "Type " + d.getName();
    }

    private String getId(EnumDeclaration d) {
        return "Enum " + d.getName();
    }

    private String getId(MethodDeclaration d) {
        StringBuilder b = new StringBuilder();
        b.append("Method ");
        b.append(d.getName() + "(");
        for (SingleVariableDeclaration v : (List<SingleVariableDeclaration>) d.parameters())
            b.append(" " + v.getType().toString());
        b.append(")");
        return b.toString();
    }

    private String getId(FieldDeclaration d) {
        return "Field " + ((VariableDeclarationFragment) d.fragments().get(0)).getName();
    }

    protected ITree getCurrentParent() {
        return trees.peek();
    }

    protected void popNode() {
        trees.pop();
    }

    protected static Type nodeAsSymbol(ASTNode node) {
        return nodeAsSymbol(node.getNodeType());
    }

    protected static Type nodeAsSymbol(int id) {
        return type(ASTNode.nodeClassForType(id).getSimpleName());
    }
}
