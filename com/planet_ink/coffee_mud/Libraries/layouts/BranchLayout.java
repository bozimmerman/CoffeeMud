package com.planet_ink.coffee_mud.Libraries.layouts;
import java.util.*;

import com.planet_ink.coffee_mud.core.Directions;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutNode;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutRuns;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutTypes;

/*
   Copyright 2025-2025 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class BranchLayout extends AbstractLayout
{
	@Override
    public String name()
    {
        return "BRANCH";
    }

	public void addRoom(final LayoutSet lSet, final LayoutNode n2, final int dir)
    {
        if (lSet.spaceAvailable())
        {
            LayoutNode nn = lSet.getNextNode(n2, dir);
            if (nn == null)
            {
                nn = lSet.makeNextNode(n2, dir);
                if(n2.type() == LayoutTypes.leaf)
                    n2.reType(LayoutTypes.street);
                lSet.use(nn, LayoutTypes.leaf);
                n2.crossLink(nn);
            }
        }
    }

	public void addBranch(final LayoutSet lSet, final LayoutNode n2, final int dir)
    {
        if (!lSet.spaceAvailable(2))
            return;

        LayoutNode nn1 = lSet.getNextNode(n2, dir);
        if (nn1 == null)
        {
            nn1 = lSet.makeNextNode(n2, dir);
            lSet.use(nn1, LayoutTypes.street);
            n2.crossLink(nn1);
        }
        else
            return;

        LayoutNode nn2 = lSet.getNextNode(nn1, dir);
        if (nn2 == null)
        {
            nn2 = lSet.makeNextNode(nn1, dir);
            lSet.use(nn2, LayoutTypes.street);
            nn1.crossLink(nn2);
        }
        else
            return;

        addRoom(lSet, nn2, dir);

        nn1.flagRun(dir == Directions.EAST || dir == Directions.WEST ? LayoutRuns.ew : LayoutRuns.ns);
        nn2.flagRun(dir == Directions.EAST || dir == Directions.WEST ? LayoutRuns.ew : LayoutRuns.ns);
    }

    @Override
    public List<LayoutNode> generate(final int num, final int dir)
    {
        final Vector<LayoutNode> set = new Vector<LayoutNode>();
        int diameter = Math.max(3, (int)Math.round(Math.sqrt(num)));
        if (diameter > num / 3)
            diameter = num / 3;
        final LayoutSet lSet = new LayoutSet(set, num);
        final int mainRooms = 2 * diameter - 1;
        int minLeaves = 3;
        final int remainingRooms = num - mainRooms - minLeaves;
        final int maxBranches = (remainingRooms / 2);
        int branchesPerCorridor = maxBranches / 2;
        if (branchesPerCorridor > diameter - 2)
            branchesPerCorridor = diameter - 2;
        if (branchesPerCorridor < 0)
            branchesPerCorridor = 0;
        minLeaves += branchesPerCorridor * 2;

        final int oppDir = Directions.getOpDirectionCode(dir);
        LayoutNode n = new DefaultLayoutNode(new long[]{0, 0});
        LayoutNode firstNode = n;
        final Vector<LayoutNode> nsCorridor = new Vector<>();
        for (int x = 0; x < diameter; x++)
        {
            lSet.use(n, LayoutTypes.street);
            n.flagRun(LayoutRuns.ns);
            nsCorridor.add(n);
            if (x < diameter - 1)
            {
                LayoutNode nn = lSet.getNextNode(n, Directions.NORTH);
                if (nn == null)
                    nn = lSet.makeNextNode(n, Directions.NORTH);
                n.crossLink(nn);
                n = nn;
            }
        }
        lSet.use(n, LayoutTypes.street);
        n.flagRun(LayoutRuns.ns);
        nsCorridor.add(n);
        if (dir != Directions.SOUTH)
            addRoom(lSet, n, Directions.NORTH);
        if (dir == Directions.NORTH)
            firstNode = nsCorridor.get(0);
        else if (dir == Directions.SOUTH)
            firstNode = n;

        n = new DefaultLayoutNode(new long[]{-(diameter / 2), -(diameter / 2)});
        if (dir == Directions.EAST)
            firstNode = n;
        final Vector<LayoutNode> ewCorridor = new Vector<>();
        for (int x = 0; x < diameter; x++)
        {
            lSet.use(n, LayoutTypes.street);
            n.flagRun(LayoutRuns.ew);
            ewCorridor.add(n);
            if (x < diameter - 1)
            {
                LayoutNode nn = lSet.getNextNode(n, Directions.EAST);
                if (nn == null)
                    nn = lSet.makeNextNode(n, Directions.EAST);
                n.crossLink(nn);
                n = nn;
            }
        }
        lSet.use(n, LayoutTypes.street);
        n.flagRun(LayoutRuns.ew);
        ewCorridor.add(n);
        if (dir != Directions.WEST)
            addRoom(lSet, n, Directions.EAST);
        if (dir != Directions.EAST)
            addRoom(lSet, ewCorridor.get(0), Directions.WEST);
        if (dir == Directions.WEST)
            firstNode = n;

        if (dir != Directions.NORTH)
            addRoom(lSet, nsCorridor.get(0), Directions.SOUTH);

        if (branchesPerCorridor > 0)
        {
            final List<Integer> availableNodes = new ArrayList<>();
            for (int i = 1; i < diameter - 1; i++)
                availableNodes.add(Integer.valueOf(i));
            Collections.shuffle(availableNodes);
            for (int i = 0; i < branchesPerCorridor && i < availableNodes.size(); i++)
            {
                final int nodeIdx = availableNodes.get(i).intValue();
                final int branchDir = Math.random() < 0.5 ? Directions.EAST : Directions.WEST;
                if (lSet.spaceAvailable(2))
                    addBranch(lSet, nsCorridor.get(nodeIdx), branchDir);
            }
        }

        if (branchesPerCorridor > 0)
        {
            final List<Integer> availableNodes = new ArrayList<>();
            for (int i = 1; i < diameter - 1; i++)
                availableNodes.add(Integer.valueOf(i));
            Collections.shuffle(availableNodes);
            for (int i = 0; i < branchesPerCorridor && i < availableNodes.size(); i++)
            {
                final int nodeIdx = availableNodes.get(i).intValue();
                final int branchDir = Math.random() < 0.5 ? Directions.NORTH : Directions.SOUTH;
                if (lSet.spaceAvailable(2))
                    addBranch(lSet, ewCorridor.get(nodeIdx), branchDir);
            }
        }

        while (lSet.spaceAvailable())
        {
            @SuppressWarnings("unchecked")
            final Vector<LayoutNode> allNodes = (Vector<LayoutNode>) set.clone();
            Collections.shuffle(allNodes);
            boolean added = false;
            for (final LayoutNode n2 : allNodes)
            {
                final int[] directions = n2.getFlagRuns() == LayoutRuns.ns ?
                    new int[]{Directions.EAST, Directions.WEST} :
                    new int[]{Directions.NORTH, Directions.SOUTH};
                for (final int d : directions)
                {
                    if (n2 == firstNode && d == oppDir)
                        continue;
                    if (lSet.getNextNode(n2, d) == null && lSet.spaceAvailable())
                    {
                        addRoom(lSet, n2, d);
                        added = true;
                    }
                }
            }
            if (!added)
                break;
        }

        lSet.fillInFlags();
        set.remove(firstNode);
        set.insertElementAt(firstNode, 0);
        return set;
    }
}