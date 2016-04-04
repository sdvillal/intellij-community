/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.codeInspection.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.ui.tree.TreeUtil;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.Update;

import javax.swing.tree.DefaultTreeModel;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Dmitry Batkovich
 */
public class InspectionTreeUpdater {
  private final MergingUpdateQueue myUpdateQueue;
  private final InspectionResultsView myView;
  private final AtomicBoolean myDoUpdatePreviewPanel = new AtomicBoolean(false);

  public InspectionTreeUpdater(InspectionResultsView view) {
    myView = view;
    myUpdateQueue = new MergingUpdateQueue("InspectionView", 100, true, view, view);
  }

  public void updateWithPreviewPanel() {
    update(false);
    myDoUpdatePreviewPanel.compareAndSet(false, true);
  }

  public void update(boolean force) {
    if (ApplicationManager.getApplication().isDispatchThread() && !force) {
      return;
    }
    myUpdateQueue.queue(new Update("TreeRepaint") {
      @Override
      public void run() {
        if (myView.isDisposed()) return;
        final InspectionTree tree = myView.getTree();
        try {
          tree.setQueueUpdate(true);
          ((DefaultTreeModel)tree.getModel()).reload();
          tree.revalidate();
          tree.repaint();
          tree.restoreExpansionAndSelection();
          if (myDoUpdatePreviewPanel.compareAndSet(true, false)) {
            myView.updateRightPanelLoading();
          }
        } finally {
          tree.setQueueUpdate(false);
          if (tree.getSelectionModel().getMinSelectionRow() == -1) {
            TreeUtil.selectFirstNode(tree);
            tree.expandRow(0);
          }
        }
      }

      @Override
      public boolean canEat(Update update) {
        return true;
      }
    });
  }
}
