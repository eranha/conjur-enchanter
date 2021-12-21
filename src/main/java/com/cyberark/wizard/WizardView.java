package com.cyberark.wizard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.cyberark.Consts.HTML_PARAGRAPH;

/**
 * This class represents the wizard view
 */
class WizardView {
  private final Icon icon;

  enum WizardNavigationCommand {
    Back,
    Next,
    Finish,
    Cancel
  }

  private final String title;
  private final Consumer<WizardNavigationCommand> actionListener;
  private final List<Page> pages;
  private JPanel contentPane;
  private JPanel pagesPane;
  private JPanel stepsLabelPane;
  private Font labelFont = (Font) UIManager.get("Label.font");
  private Font boldLabelFont = labelFont.deriveFont(labelFont.getStyle() | Font.BOLD);
  private int pageIndex;
  private CardLayout cardLayout;
  private final Map<WizardNavigationCommand, JButton> navigationButtons = new HashMap<>();
  private final Map<String, JLabel> steplabels = new HashMap<>();
  private JDialog dlg;


  WizardView(Icon icon, String title, Consumer<WizardNavigationCommand> actionListener, List<Page> pages) {
    this.icon = icon;
    this.title = title;
    this.actionListener = actionListener;
    this.pages = pages;

    initializeComponenets();
  }

  private void initializeComponenets() {
    cardLayout = new CardLayout();
    contentPane = new JPanel(new BorderLayout());
    pagesPane = new JPanel(cardLayout);
    stepsLabelPane = new JPanel(new BorderLayout());
    stepsLabelPane.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(8,8,8, 8),
        BorderFactory.createEmptyBorder()));

    JPanel labelsPanel = new JPanel();
    BoxLayout boxLayout = new BoxLayout(labelsPanel, BoxLayout.Y_AXIS);
    int pad = 16;
    labelsPanel.setLayout(boxLayout);
    labelsPanel.setBorder(BorderFactory.createEmptyBorder(pad, pad * 2, pad, pad * 2));
    stepsLabelPane.add(labelsPanel, BorderLayout.CENTER);

    JPanel leftPane = new JPanel(new BorderLayout());
    //leftPane.setBorder(BorderFactory.createLineBorder(Color.black));
    leftPane.add(stepsLabelPane, BorderLayout.NORTH);
    leftPane.add(Box.createVerticalGlue(), BorderLayout.CENTER);

    if (icon != null) {
      JLabel iconLabel = new JLabel(icon, JLabel. CENTER);
      labelsPanel.add(iconLabel);
      labelsPanel.add(Box.createVerticalStrut(16));

    }

    pages.forEach(p ->
        {
          JLabel label = new JLabel(p.getTitle());
          steplabels.put(p.getId(), label);
          labelsPanel.add(label);
          labelsPanel.add(Box.createRigidArea(new Dimension(1, 16)));

          JPanel infoPane = new JPanel();
          BoxLayout boxLayout2 = new BoxLayout(infoPane, BoxLayout.Y_AXIS);
          infoPane.setLayout(boxLayout2);

          JPanel pane = new JPanel(new BorderLayout());
          pane.setBorder(BorderFactory.createCompoundBorder(
              BorderFactory.createEmptyBorder(8,8,8, 16),
              BorderFactory.createEmptyBorder()));

          infoPane.add(Box.createVerticalStrut(12));
          infoPane.add(new JLabel(String.format(HTML_PARAGRAPH, 400, p.getDescription())));
          infoPane.add(Box.createVerticalStrut(12));
          JPanel hr = new JPanel();
          hr.setBorder(BorderFactory.createLineBorder(Color.lightGray));
          hr.setPreferredSize(new Dimension(1,2));
          infoPane.add(hr);
          infoPane.add(Box.createVerticalStrut(12));

          pane.add(infoPane, BorderLayout.NORTH);
          pane.add(p.getPageView(), BorderLayout.CENTER);
          pagesPane.add(pane, p.getId());
        }
    );

    contentPane.add(leftPane, BorderLayout.WEST);

    JPanel vhr = new JPanel();
    vhr.setBorder(BorderFactory.createLineBorder(Color.lightGray));
    vhr.setPreferredSize(new Dimension(1,1));

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(vhr, BorderLayout.WEST);
    mainPanel.add(pagesPane, BorderLayout.CENTER);

    contentPane.add(mainPanel, BorderLayout.CENTER);

    JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0,0));
    buttonsPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 8));
    buttonsPanel.add(createWizardButton("< Back", WizardNavigationCommand.Back));
    buttonsPanel.add(createWizardButton("Next >", WizardNavigationCommand.Next));
    buttonsPanel.add(createWizardButton("Finish", WizardNavigationCommand.Finish));
    buttonsPanel.add(createWizardButton("Cancel", WizardNavigationCommand.Cancel));
    JPanel buttonsPane = new JPanel(new BorderLayout());
    JPanel hr = new JPanel();
    hr.setBorder(BorderFactory.createLineBorder(Color.lightGray));
    hr.setPreferredSize(new Dimension(1,2));
    buttonsPane.add(hr, BorderLayout.NORTH );
    buttonsPane.add(buttonsPanel, BorderLayout.CENTER);
    contentPane.add(buttonsPane, BorderLayout.SOUTH);
  }

  private JButton createWizardButton(String text, WizardNavigationCommand command) {
    JButton b = new JButton(text);
    b.addActionListener(e -> actionListener.accept(command));
    navigationButtons.put(command, b);
    return b;
  }

  void setPageIndex(int index) {
    // reset font of current step
    setStepLableFont(pageIndex, labelFont);
    pageIndex = index;
    // bold the new step
    setStepLableFont(pageIndex, boldLabelFont);
    cardLayout.show(pagesPane, pages.get(pageIndex).getId());
  }

  void setStepLableFont(int pageIndex, Font font) {
    steplabels.get(pages.get(pageIndex).getId()).setFont(font);
  }

  void toggleNavigationCommand(WizardNavigationCommand command, boolean enabled) {
    navigationButtons.get(command).setEnabled(enabled);
  }

  void showDialog(Frame owner) {
    dlg = new JDialog(owner, title, true);
    dlg.getContentPane().add(contentPane);
    dlg.getRootPane().setDefaultButton(navigationButtons.get(WizardNavigationCommand.Finish));
    dlg.pack();
    dlg.setLocationRelativeTo(owner);
    dlg.getRootPane().registerKeyboardAction(e -> hideDialog(),
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        JComponent.WHEN_IN_FOCUSED_WINDOW);
    dlg.setVisible(true);
  }

  void hideDialog() {
    if (dlg != null) {
      dlg.setVisible(false);
      dlg.dispose();
    }
  }
}
