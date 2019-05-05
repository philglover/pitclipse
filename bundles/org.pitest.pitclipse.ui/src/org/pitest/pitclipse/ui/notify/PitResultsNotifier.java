package org.pitest.pitclipse.ui.notify;

import org.eclipse.swt.widgets.Display;
import org.pitest.pitclipse.core.extension.point.ResultNotifier;
import org.pitest.pitclipse.runner.PitResults;
import org.pitest.pitclipse.ui.view.PitViewFinder;
import org.pitest.pitclipse.ui.view.SummaryView;

public class PitResultsNotifier implements ResultNotifier<PitResults> {
    @Override
    public void handleResults(PitResults results) {
        Display.getDefault().asyncExec(() -> {
            SummaryView view = PitViewFinder.INSTANCE.getSummaryView();
            view.update(results.getHtmlResultFile());
        });
    }
}
