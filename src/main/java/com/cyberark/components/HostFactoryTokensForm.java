package com.cyberark.components;

import com.cyberark.models.hostfactory.HostFactoryTokensFormModel;
import com.cyberark.models.table.StringTableModel;

import javax.swing.*;
import java.awt.*;

public class HostFactoryTokensForm extends ContainerBase {
  private final HostFactoryTokensFormModel model;
  private final StringTableModel restrictionsModel = new StringTableModel();

  public HostFactoryTokensForm(String hostFactoryId) {
    this.model = new HostFactoryTokensFormModel(hostFactoryId);
    initializeComponents();
  }

  private void initializeComponents() {
    EditableTableImpl<String> table = new EditableTableImpl<>(
        restrictionsModel, m -> getString("default.restriction.ip"), false
    );

    setLayout(new GridBagLayout());
    table.setPreferredSize(new Dimension(300, 100));
    int gridy = 0;

    // Number of Tokens
    add(new JLabel(getString("tokens.form.number.of.tokens")),
      new GridBagConstraints(
        0, gridy, 1, 1, 0, 0,
        GridBagConstraints.WEST,
        GridBagConstraints.NONE,
        new Insets(0,0,0,16), 0, 0
      )
    );

    JSpinner numOfTokenSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
    numOfTokenSpinner.addChangeListener(
        e -> model.setNumberOfTokens((Integer) numOfTokenSpinner.getValue())
    );

    add(numOfTokenSpinner,
      new GridBagConstraints(
          1, gridy++, 1, 1, 0, 0,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(0,0,0,0), 0, 0
      )
    );

    // Spacer
    add(Box.createVerticalStrut(8),
        new GridBagConstraints(
            0, gridy++, 1, 1, 0, 0,
            GridBagConstraints.CENTER,
            GridBagConstraints.NONE,
            new Insets(0,0,0,0), 0, 0
        )
    );

    // Expiration
    add(new JLabel(getString("tokens.form.expiration")),
        new GridBagConstraints(
            0, 2, 1, 1, 0, 0,
            GridBagConstraints.WEST,
            GridBagConstraints.NONE,
            new Insets(0,0,0,16), 0, 0
        )
    );

    JPanel panel = new JPanel();
    JSpinner daysSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 30, 1));
    JSpinner hoursSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 24, 1));
    JSpinner minutesSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 60, 1));

    daysSpinner.addChangeListener(e -> model.setExpirationDays((Integer) daysSpinner.getValue()));
    hoursSpinner.addChangeListener(e -> model.setExpirationHours((Integer) hoursSpinner.getValue()));
    minutesSpinner.addChangeListener(e -> model.setExpirationMinutes((Integer) minutesSpinner.getValue()));

    panel.add(new JLabel(getString("tokens.form.expiration.days")));
    panel.add(daysSpinner);
    panel.add(new JLabel(getString("tokens.form.expiration.hours")));
    panel.add(hoursSpinner);
    panel.add(new JLabel(getString("tokens.form.expiration.minutes")));
    panel.add(minutesSpinner);
    add(panel,
      new GridBagConstraints(
          1, gridy++, 1, 1, 0, 0,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(0,0,0,0), 0, 0
      )
    );

    // Spacer
    add(Box.createVerticalStrut(8),
        new GridBagConstraints(
            0, gridy++, 1, 1, 0, 0,
            GridBagConstraints.CENTER,
            GridBagConstraints.NONE,
            new Insets(0,0,0,0), 0, 0
        )
    );

    // Restrictions
    add(new JLabel(getString("restrictions.label.text")),
        new GridBagConstraints(
            0, gridy, 1, 1, 0, 0,
            GridBagConstraints.NORTHWEST,
            GridBagConstraints.NONE,
            new Insets(0,0,0,0), 0, 0
        )
    );
    add(table,
        new GridBagConstraints(
            1, gridy, 1, 1, 1, 1,
            GridBagConstraints.CENTER,
            GridBagConstraints.BOTH,
            new Insets(0,4,0,5), 0, 0
        )
    );
  }

  public HostFactoryTokensFormModel getModel() {
    model.setRestrictions(restrictionsModel.getItems());
    return model;
  }
}
