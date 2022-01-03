package com.cyberark.wizard;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static com.cyberark.wizard.WizardView.WizardNavigationCommand.*;

/**
 * This class represents the wizard controller
 */
public class Wizard {
  public static final int CANCEL_OPTION = 2;
  public static final int OK_OPTION = 0;

  private final String title;
  private final PageEventListener eventListener;
  private final List<Page> pages;
  private final Icon icon;
  private int pageIndex;
  private WizardView view;
  private Supplier<Boolean> canFinish;
  private int result = CANCEL_OPTION;

  public Wizard(String title,
                PageEventListener eventListener,
                List<Page> pages,
                Supplier<Boolean>  canFinish) {
    this(null, title, eventListener, pages, canFinish);
  }

  public Wizard(Icon icon,
                String title,
                PageEventListener eventListener,
                List<Page> pages,
                Supplier<Boolean>  canFinish) {
    this.icon = icon;
    this.title = title;
    this.eventListener = eventListener;
    this.pages = pages;
    this.canFinish = canFinish;
  }

  public int showWizard(Frame owner) {
    view = new WizardView(icon, title, this::handleNavigationEvent, pages);
    view.toggleNavigationCommand(Back, false);
    view.toggleNavigationCommand(Next, pages.size() > 1 &&
        pages.stream().skip(pageIndex).noneMatch(Page::isMandatory));

    toggleFinishButton();

    if (pages.size() > 0) {
      view.setPageIndex(0);
    } else {
      Arrays.stream(values()).filter(i-> i != Cancel).forEach(i -> view.toggleNavigationCommand(i, false));
    }

    view.showDialog(owner);
    return result;
  }

  private void handleNavigationEvent(WizardView.WizardNavigationCommand command) {
    if (command != Cancel && pages.isEmpty()) return;
    eventListener.pageEvent(new PageEvent(PageEvent.EventType.AboutToHidePage, pages.get(pageIndex)));

    switch (command) {
      case Next:
        if (pageIndex < pages.size() - 1) {
          pageIndex++;
        }
        break;
      case Back:
        pageIndex--;
        break;
      case Cancel:
        view.hideDialog();
        break;
      case Finish:
        result = JOptionPane.OK_OPTION;
        view.hideDialog();
        break;
    }

    eventListener.pageEvent(new PageEvent(
        command == Finish
          ? PageEvent.EventType.AboutToShowPage
          : PageEvent.EventType.AboutToFinish , pages.get(pageIndex)));

    view.setPageIndex(pageIndex);
    view.toggleNavigationCommand(Next, pages.size() > 1 && pageIndex < pages.size() - 1);
    view.toggleNavigationCommand(Back, pageIndex > 0);
    toggleFinishButton();
  }

  private void toggleFinishButton() {
    boolean flag = canFinish.get() &&
        pages
          .stream()
          .skip(pageIndex)
          .noneMatch(Page::isMandatory);
    view.toggleNavigationCommand(Finish, flag);
  }

  public void toggleFinishButton(boolean flag) {
    view.toggleNavigationCommand(Finish, flag);
  }
}
